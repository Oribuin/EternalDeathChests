package xyz.oribuin.deathchests.hologram

import xyz.oribuin.deathchests.util.locationAsKey
import xyz.oribuin.deathchests.hologram.HologramHandler
import me.gholo.api.GHoloAPI
import me.gholo.objects.Holo
import org.bukkit.Location
import org.bukkit.entity.Entity
import java.util.*
import java.util.function.Consumer

/**
 * @author Esophose
 * happily yoinked
 */
class GHoloHandler : HologramHandler {
    private val api: GHoloAPI = GHoloAPI()

    private val locations = mutableSetOf<Location>()

    override fun createOrUpdateHologram(location: Location, text: List<String>) {
        val key = locationAsKey(location)
        if (api.getHolo(key) == null) {
            api.insertHolo(key, location.clone().add(0.0, 0.65, 0.0), text)
            locations.add(location)
        } else {
            api.setHoloContent(key, text)
        }
    }

    override fun deleteHologram(location: Location) {
        val key = locationAsKey(location)
        api.removeHolo(key)
        locations.remove(location)
    }

    override fun deleteAllHolograms() {
        HashSet(locations).forEach(Consumer { location: Location -> deleteHologram(location) })
    }

    override fun isHologram(entity: Entity): Boolean {
        return locations.stream().map { x: Location? -> api.getHolo(locationAsKey(x!!)) }
            .filter { obj: Holo? -> Objects.nonNull(obj) }
            .map { obj: Holo -> obj.uuiDs }
            .flatMap { obj: List<UUID> -> obj.stream() }
            .anyMatch { x: UUID -> x == entity.uniqueId }
    }

    override val isEnabled: Boolean
        get() = true

}