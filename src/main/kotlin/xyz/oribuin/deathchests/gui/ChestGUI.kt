package xyz.oribuin.deathchests.gui

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import xyz.oribuin.deathchests.EternalDeathChests
import xyz.oribuin.deathchests.chest.DeathChest
import xyz.oribuin.deathchests.util.applyPapi
import xyz.oribuin.gui.Gui
import xyz.oribuin.gui.Item
import xyz.oribuin.orilibrary.util.HexUtils.colorify
import xyz.oribuin.orilibrary.util.StringPlaceholders
import java.util.function.Consumer
import kotlin.math.max

class ChestGUI(private val plugin: EternalDeathChests, private val chest: DeathChest, player: Player) {

    private val config = this.plugin.config

    init {
        val gui = Gui(54, this.get("deathchest-gui.title", applyPapi(chest.offlinePlayer, "%player_name%'s Death Chest")))

        val clickableSlots = mutableListOf<Int>()
        for (i in 45..53)
            clickableSlots.add(i)

        gui.setDefaultClickFunction {
            if (!clickableSlots.contains(it.slot))
                return@setDefaultClickFunction

            it.isCancelled = true
            it.result = Event.Result.DENY
            (it.whoClicked as Player).updateInventory()
        }

        gui.setOpenAction {
            chest.update()
            chest.guiOpened = true
        }

        gui.setCloseAction {
            chest.deathItems.clear()

            for (i in 0..44) {
                val item = gui.inv.getItem(i) ?: continue
                if (item.type != Material.AIR)
                    chest.deathItems.add(item)
            }

            if (chest.deathItems.size == 0) {
                chest.remove()
                return@setCloseAction
            }

            chest.guiOpened = false
            chest.update()
        }



        clickableSlots.forEach { gui.setItem(it, Item.filler(Material.BLUE_STAINED_GLASS_PANE)) }

        this.plugin.server.scheduler.runTaskTimer(plugin, Consumer {
            if (!chest.guiOpened) {
                it.cancel()
                return@Consumer
            }
            val placeholders = StringPlaceholders.builder()
                .addPlaceholder("expiry", chest.expireTime())
                .addPlaceholder("items", chest.deathItems.size)
                .addPlaceholder("xp", chest.savedXP)
                .build()

            if (this.get("deathchest-gui.expire-timer.enabled", true)) {
                gui.setItem(46, this.itemFromConfig("deathchest-gui.expire-timer", placeholders)) { }
            }

            gui.update()
        }, 2, 15)


        chest.deathItems.forEach { gui.addItem(it) {} }
        gui.open(player)
    }

    /**
     * Get an itemstack from the config.
     *
     * @param path The path to the item
     * @param pl The placeholders for any strings
     * @return The new itemstack.
     */
    private fun itemFromConfig(path: String, pl: StringPlaceholders = StringPlaceholders.empty()): ItemStack {
        val material = Material.matchMaterial(get("$path.material", "BARRIER")) ?: Material.BARRIER

        val name = applyPapi(chest.offlinePlayer, colorify(get("$path.name", "&cInvalid Name: $path")), pl)
        val lore = get("$path.lore", listOf("&cInvalid Lore: $path"))
            .map { applyPapi(chest.offlinePlayer, colorify(it), pl) }

        val amount = max(get("$path.amount", 1), 1)
        val glowing = get("$path.glow", true)
        val texture = get("$path.texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGZkNWJkZTk5NGUwYTY0N2FmMTgyMzY4MWE2MTNjMmJmYzNkOTczNmY4ODlkYmY4YzNiYmJhNWExM2Y4ZWQifX19")
        return Item.Builder(material)
            .setName(name)
            .setLore(lore)
            .glow(glowing)
            .setTexture(texture)
            .setAmount(amount)
            .create()
    }


    /**
     * Get a config value or the default value from the config.
     *
     * @param path The path to the config.
     * @param default The default option for the config value.
     * @suppress Unchecked Cats
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(path: String, default: T): T {
        return (config.get(path) as T) ?: default
    }

}