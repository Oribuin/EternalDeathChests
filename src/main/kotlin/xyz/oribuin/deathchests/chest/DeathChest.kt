package xyz.oribuin.deathchests.chest

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.OfflinePlayer
import org.bukkit.entity.ArmorStand
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import xyz.oribuin.deathchests.EternalDeathChests
import xyz.oribuin.deathchests.manager.ChestManager
import xyz.oribuin.deathchests.manager.HologramManager
import xyz.oribuin.deathchests.util.formatMillis
import xyz.oribuin.deathchests.util.getBlockLoc
import xyz.oribuin.orilibrary.util.HexUtils
import xyz.oribuin.orilibrary.util.StringPlaceholders
import java.util.*
import java.util.stream.Collectors

class DeathChest(private val plugin: EternalDeathChests, var player: UUID, val location: Location) {

    var deathItems = mutableListOf<ItemStack>()
    var spawnTime = System.currentTimeMillis()
    var armorStand: ArmorStand? = null
    var savedXP: Int = 0
    var guiOpened: Boolean = false
    var holoLocation: Location? = null
    var exists: Boolean = true

    private val manager = this.plugin.getManager(ChestManager::class.java)

    /**
     * Spawn in a death chest in for the plugin
     *
     * @return true if the chest was created.
     */
    fun create(): Boolean {
        val loc = this.location.clone().subtract(0.0, 1.5, 0.0)

        val player = Bukkit.getPlayer(this.player) ?: return false

        this.createOrUpdateHolo()
        this.spawnTime = System.currentTimeMillis()
        this.armorStand = manager.makeArmorstand(player, loc, this)
        this.exists = true

        return true
    }

    /**
     * Create a hologram.
     */
    fun createOrUpdateHolo() {
        val holoManager = plugin.getManager(HologramManager::class.java)

        if (!holoManager.isHologramsEnabled)
            return

        val placeholders = StringPlaceholders.builder()
            .addPlaceholder("player_name", this.offlinePlayer.name)
            .addPlaceholder("expiry", this.expireTime())
            .build()

        // yes this is scuffed
        val holoLocation = this.holoLocation ?: this.location.clone().add(0.0, 0.5, 0.0)

        this.holoLocation = holoLocation
        holoManager.createOrUpdateHologram(holoLocation, holoMessage.map { placeholders.apply(it) })
    }

    /**
     * Remove a death chest from the world and cache.
     */
    fun remove() {
        val loc = getBlockLoc(this.location)
        manager.cachedChests.remove(loc)
        this.armorStand?.remove()
        this.exists = false

        plugin.getManager(HologramManager::class.java).deleteHologram(holoLocation ?: return)
    }

    /**
     * Update the chest's current armorstand PDC values.
     */
    fun update(stand: ArmorStand? = null) {
        // oh, boy this is something
        val cont = (stand ?: this.armorStand ?: return).persistentDataContainer

        // Set PDC Values
        cont.set(NamespacedKey(plugin, "player"), PersistentDataType.STRING, player.toString())
        cont.set(NamespacedKey(plugin, "items"), PersistentDataType.BYTE_ARRAY, manager.compressItemStacks(deathItems))
        cont.set(NamespacedKey(plugin, "spawnTime"), PersistentDataType.LONG, spawnTime)
        cont.set(NamespacedKey(plugin, "xp"), PersistentDataType.INTEGER, savedXP)
        cont.set(NamespacedKey(plugin, "hololoc"), PersistentDataType.STRING, manager.serializeLocation(this.holoLocation ?: return))

    }

    /**
     * Check if the death chest has expired.
     *
     * @return true if expired.
     */
    fun expired(): Boolean {
        val expiryTime = plugin.config.getInt("deathchest-settings.expiry-time")

        // current time > spawnTime + (expiryTime * 1000)
        return System.currentTimeMillis() > spawnTime + (expiryTime * 1000)
    }

    fun expireTime(): String {
        val expiryTime = plugin.config.getInt("deathchest-settings.expiry-time")

        if (this.expired())
            return "Expired"

        return formatMillis(spawnTime + (expiryTime * 1000) - System.currentTimeMillis())
    }

    val offlinePlayer: OfflinePlayer
        get() = Bukkit.getOfflinePlayer(this.player)

    private val holoMessage = plugin.config
        .getStringList("deathchest-settings.hologram.message")
        .stream()
        .map { HexUtils.colorify(it) }
        .collect(Collectors.toList())
}