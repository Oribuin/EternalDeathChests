package xyz.oribuin.deathchests.hologram

import com.sainttx.holograms.api.Hologram
import com.sainttx.holograms.api.HologramEntityController
import com.sainttx.holograms.api.HologramManager
import com.sainttx.holograms.api.HologramPlugin
import com.sainttx.holograms.api.line.TextLine
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.java.JavaPlugin
import xyz.oribuin.deathchests.util.locationAsKey

/**
 * @author Esophose
 * happily yoinked
 */
class SaintHoloHandler : HologramHandler {

    private val hologramManager: HologramManager
    private val hologramEntityController: HologramEntityController
    private val holograms = mutableSetOf<String>()

    override fun createOrUpdateHologram(location: Location, text: List<String>) {
        val key = locationAsKey(location)
        var hologram = hologramManager.getHologram(key)
        if (hologram == null) {
            hologram = Hologram(locationAsKey(location), location.clone().add(0.0, 0.5, 0.0))
            val finalHologram1 = hologram
            text.stream().map { TextLine(finalHologram1, it) }.forEach { hologram.addLine(it) }
            hologramManager.addActiveHologram(hologram)
            holograms.add(key)
        } else {
            for (line in ArrayList(hologram.lines)) hologram.removeLine(line)
            val finalHologram = hologram
            text.stream().map { s: String? -> TextLine(finalHologram, s) }.forEach { line: TextLine? -> hologram.addLine(line) }
        }
    }

    override fun deleteHologram(location: Location) {
        val key = locationAsKey(location)
        val hologram = hologramManager.getHologram(key)
        if (hologram != null) {
            hologram.despawn()
            hologramManager.deleteHologram(hologram)
        }
        holograms.remove(key)
    }

    override fun deleteAllHolograms() {
        for (key in holograms) {
            val hologram = hologramManager.getHologram(key)
            if (hologram != null) {
                hologram.despawn()
                hologramManager.removeActiveHologram(hologram)
            }
        }
        holograms.clear()
    }

    override fun isHologram(entity: Entity): Boolean {
        val hologramEntity = hologramEntityController.getHologramEntity(entity) ?: return false
        val hologramLine = hologramEntity.hologramLine ?: return false
        val hologram = hologramLine.hologram ?: return false
        val id = hologram.id ?: return false
        return holograms.stream().anyMatch { id == it }
    }

    override val isEnabled: Boolean
        get() = true

    init {
        val hologramPlugin = JavaPlugin.getPlugin(HologramPlugin::class.java)
        hologramManager = hologramPlugin.hologramManager
        hologramEntityController = hologramPlugin.entityController
    }
}