package xyz.oribuin.deathchests.hologram

import org.bukkit.Location
import org.bukkit.entity.Entity

/**
 * @author Esophose
 * happily yoinked
 */
interface HologramHandler {
    /**
     * Creates or updates a hologram at the given location
     *
     * @param location The location of the hologram
     * @param text     The text for the hologram
     */
    fun createOrUpdateHologram(location: Location, text: List<String>)

    /**
     * Deletes a hologram at a given location if one exists
     *
     * @param location The location of the hologram
     */
    fun deleteHologram(location: Location)

    /**
     * Deletes all holograms
     */
    fun deleteAllHolograms()

    /**
     * Checks if the given Entity is part of a hologram
     *
     * @param entity The Entity to check
     * @return true if the Entity is a hologram, otherwise false
     */
    fun isHologram(entity: Entity): Boolean

    /**
     * @return true if this hologram handler plugin is enabled, false otherwise
     */
    val isEnabled: Boolean
}