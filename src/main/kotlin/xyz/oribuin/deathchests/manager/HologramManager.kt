package xyz.oribuin.deathchests.manager

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import xyz.oribuin.deathchests.EternalDeathChests
import xyz.oribuin.deathchests.hologram.*
import xyz.oribuin.orilibrary.manager.Manager

/**
 * @author Esophose
 */
class HologramManager(private val plugin: EternalDeathChests) : Manager(plugin) {

    private var holoHandlers = mutableMapOf<String, Class<out HologramHandler>>()
    private var handler: HologramHandler? = null
    var isHologramsEnabled = true

    override fun enable() {
        isHologramsEnabled = this.plugin.config.getBoolean("deathchest-settings.hologram.enabled")

        if (!isHologramsEnabled)
            return

        Bukkit.getScheduler().runTaskLater(this.plugin, Runnable {
            if (attemptLoad())
                return@Runnable

            val validPlugins = java.lang.String.join(" ", holoHandlers.keys)
            this.plugin.logger.warning("We could not find a supported Hologram Plugin so holograms will not be enabled. We support [$validPlugins]")
        }, 1)
    }

    override fun disable() {
        if (handler != null) {
            handler?.deleteAllHolograms()
            handler = null
        }
    }

    /**
     * Creates or updates a hologram at the given location
     *
     * @param location The location of the hologram
     * @param text The text for the hologram
     */
    fun createOrUpdateHologram(location: Location, text: List<String>) {
        if (handler != null) handler?.createOrUpdateHologram(location, text)
    }

    /**
     * Deletes a hologram at a given location if one exists
     *
     * @param location The location of the hologram
     */
    fun deleteHologram(location: Location) {
        if (handler != null) handler?.deleteHologram(location)
    }

    /**
     * Deletes all holograms
     */
    fun deleteAllHolograms() {
        if (handler != null) handler?.deleteAllHolograms()
    }

    /**
     * Checks if the given Entity is part of a hologram
     *
     * @param entity The Entity to check
     * @return true if the Entity is a hologram, otherwise false
     */
    fun isHologram(entity: Entity): Boolean {
        return (handler ?: return false).isHologram(entity)
    }

    /**
     * Attempt to load the hologram manager from the existing plugins.
     *
     * @return true if there was a hologram loaded.
     */
    private fun attemptLoad(): Boolean {
        for ((key, value) in holoHandlers) {
            if (!Bukkit.getPluginManager().isPluginEnabled(key))
                continue

            try {
                val handler = value.getConstructor().newInstance()
                this.handler = handler
                if (!handler.isEnabled)
                    continue

                this.plugin.logger.info("Found & Loaded Hologram support for: $key")
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    init {

        // major credit to esophose for this lmao
        // I would not and could not be bothered to do this on my own
        holoHandlers = object : LinkedHashMap<String, Class<out HologramHandler>>() {
            init {
                this["HolographicDisplays"] = FiloHoloHandler::class.java
                this["Holograms"] = SaintHoloHandler::class.java
                this["GHolo"] = GHoloHandler::class.java
                this["CMI"] = CMIHoloHandler::class.java
            }
        }
    }
}