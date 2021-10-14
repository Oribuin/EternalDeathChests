package xyz.oribuin.deathchests.hologram

import com.Zrips.CMI.CMI
import com.Zrips.CMI.Modules.Holograms.CMIHologram
import com.Zrips.CMI.Modules.Holograms.HologramManager
import com.Zrips.CMI.Modules.ModuleHandling.CMIModule
import net.Zrips.CMILib.Container.CMILocation
import org.bukkit.Location
import org.bukkit.entity.Entity
import xyz.oribuin.deathchests.util.locationAsKey

/**
 * @author Esophose
 * happily yoinked
 */
class CMIHoloHandler : HologramHandler {
    private val manager: HologramManager = CMI.getInstance().hologramManager
    private val holograms = mutableMapOf<Location, CMIHologram>()

    override fun createOrUpdateHologram(location: Location, text: List<String>) {
        var hologram = holograms[location]

        if (hologram == null) {
            hologram = CMIHologram(locationAsKey(location), CMILocation(location.clone().add(0.0, 1.0, 0.0)))
            hologram.lines = text
            manager.addHologram(hologram)
            holograms[location] = hologram
        } else {
            hologram.lines = text
        }

        hologram.update()
    }

    override fun deleteHologram(location: Location) {
        val hologram = holograms[location]
        if (hologram != null) {
            manager.removeHolo(hologram)
            holograms.remove(location)
        }
    }

    override fun deleteAllHolograms() {
        HashSet(holograms.keys).forEach { location -> deleteHologram(location) }
    }

    override fun isHologram(entity: Entity): Boolean {
        return false // CMI Holograms appear to use packets and therefore do not use entities
    }

    override val isEnabled: Boolean
        get() = CMIModule.holograms.isEnabled

}