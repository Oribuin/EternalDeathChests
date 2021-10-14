package xyz.oribuin.deathchests

import xyz.oribuin.deathchests.listener.ChunkListeners
import xyz.oribuin.deathchests.listener.PlayerListeners
import xyz.oribuin.deathchests.manager.ChestManager
import xyz.oribuin.deathchests.manager.HologramManager
import xyz.oribuin.orilibrary.OriPlugin

class EternalDeathChests : OriPlugin() {

    override fun enablePlugin() {
        instance = this

        // Load Managers
        getManager(ChestManager::class.java)
        getManager(HologramManager::class.java)

        // Register Plugin listeners
        PlayerListeners(this)
        ChunkListeners(this)
    }

    override fun disablePlugin() {

    }

    companion object {
        lateinit var instance: EternalDeathChests
    }

}