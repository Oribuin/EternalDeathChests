package xyz.oribuin.deathchests.hologram

import com.gmail.filoghost.holographicdisplays.HolographicDisplays
import com.gmail.filoghost.holographicdisplays.api.Hologram
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI
import org.bukkit.Location
import org.bukkit.entity.Entity
import xyz.oribuin.deathchests.EternalDeathChests
import java.util.function.Consumer

class FiloHoloHandler : HologramHandler {

    private val holograms = mutableMapOf<Location, Hologram>()

    override fun createOrUpdateHologram(location: Location, text: List<String>) {
        var hologram = holograms[location]
        if (hologram == null) {
            hologram = HologramsAPI.createHologram(EternalDeathChests.instance, location.clone().add(0.0, 1.0, 0.0))
            val finalHologram = hologram
            text.forEach { finalHologram.appendTextLine(it) }
            holograms[location] = finalHologram
        } else {
            hologram.clearLines()
            for (i in text.indices) hologram.insertTextLine(i, text[i])
        }
    }

    override fun deleteHologram(location: Location) {
        val hologram = holograms[location]
        if (hologram != null) {
            hologram.delete()
            holograms.remove(location)
        }
    }

    override fun deleteAllHolograms() {
        HashSet(holograms.keys).forEach(Consumer { location: Location -> deleteHologram(location) })
    }

    override fun isHologram(entity: Entity): Boolean {
        val entityBase = HolographicDisplays.getNMSManager().getNMSEntityBase(entity) ?: return false
        val target = entityBase.hologramLine.parent ?: return false
        return holograms.values.stream().anyMatch { target == it }
    }

    override val isEnabled: Boolean
        get() = true

}