package xyz.oribuin.deathchests.listener

import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import xyz.oribuin.deathchests.EternalDeathChests
import xyz.oribuin.deathchests.chest.DeathChest
import xyz.oribuin.deathchests.gui.ChestGUI
import xyz.oribuin.deathchests.manager.ChestManager

class PlayerListeners(private val plugin: EternalDeathChests) : Listener {

    private val manager = plugin.getManager(ChestManager::class.java)

    @EventHandler(ignoreCancelled = true)
    fun PlayerDeathEvent.onDeath() {
        if (this.keepInventory) return

        val chest = DeathChest(plugin, this.entity.uniqueId, this.entity.location)
        chest.deathItems = this.drops.toMutableList()
        chest.savedXP = this.droppedExp

        this.drops.clear()
        this.droppedExp = 0
        chest.create()
    }

    @EventHandler
    // I do hate this event but apparently armorstands don't trigger PlayerInteractEntityEvent
    fun PlayerInteractAtEntityEvent.onInteract() {

        if (this.rightClicked !is ArmorStand)
            return

        val entity = this.rightClicked as ArmorStand

        val chest = manager.getChest(entity)
        if (chest.isEmpty)
            return

        this.isCancelled = true
        ChestGUI(plugin, chest.get(), this.player)
    }

    @EventHandler
    fun EntityDamageEvent.onEntityDeath() {
        if (this.entity !is ArmorStand)
            return

        manager.getChest(this.entity as ArmorStand).ifPresent { this.isCancelled = true }
    }

    init {
        this.plugin.server.pluginManager.registerEvents(this, plugin)
    }
}