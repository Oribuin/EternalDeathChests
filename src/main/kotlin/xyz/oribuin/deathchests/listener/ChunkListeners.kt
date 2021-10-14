package xyz.oribuin.deathchests.listener

import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import xyz.oribuin.deathchests.EternalDeathChests
import xyz.oribuin.deathchests.manager.ChestManager

class ChunkListeners(private val plugin: EternalDeathChests) : Listener {

    private val manager = this.plugin.getManager(ChestManager::class.java)

    @EventHandler
    fun ChunkLoadEvent.onLoad() {
        chunk.entities.filterIsInstance<ArmorStand>().forEach { manager.getChest(it) }
    }

    init {
        this.plugin.server.pluginManager.registerEvents(this, plugin)
    }

}