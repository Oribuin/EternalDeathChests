package xyz.oribuin.deathchests.util

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import xyz.oribuin.orilibrary.util.StringPlaceholders
import kotlin.math.floor

/**
 * Format milliseconds into a readable text
 *
 * @param millis The milliseconds in time
 * @return The formatted time
 */
fun formatMillis(millis: Long): String {
    var totalSeconds = millis / 1000

    if (totalSeconds <= 0)
        return ""

    val days = floor(totalSeconds / 86400.0).toInt()
    totalSeconds %= 86400

    val hours = floor(totalSeconds / 3600.0).toInt()
    totalSeconds %= 3600

    val mins = floor(totalSeconds / 60.0).toInt()
    val secs = totalSeconds % 60

    val builder = StringBuilder()
    if (days > 0)
        builder.append(days).append("d, ")

    if (hours > 0)
        builder.append(hours).append("h, ")

    if (mins > 0)
        builder.append(mins).append("m, ")

    builder.append(secs).append("s")
    return builder.toString()
}

/**
 * Format a string list into a single string.
 *
 * @param stringList The strings being converted
 * @return the converted string.
 */
fun formatList(stringList: List<String>): String {
    val builder = StringBuilder()
    stringList.forEach { builder.append(it).append("\n") }
    return builder.toString()
}

/**
 * Format a location into a readable String.
 *
 * @param loc The location
 * @return The formatted Location.
 */
fun formatLocation(loc: Location): String {
    return loc.blockX.toString() + ", " + loc.blockY + ", " + loc.blockZ
}

/**
 * Get the block location of the location.;
 *
 * @param loc The location;
 * @return The block location
 */
fun getBlockLoc(loc: Location): Location {
    val location = loc.clone()
    return Location(location.world, location.blockX.toDouble(), loc.blockY.toDouble(), loc.blockZ.toDouble())
}

/**
 * Gets a location as a string key
 *
 * @author Esophose
 * @param location The location
 * @return the location as a string key
 */
fun locationAsKey(location: Location): String {
    return String.format("%s-%.2f-%.2f-%.2f", location.world?.name, location.x, location.y, location.z)
}

/**
 * Center a location to the center of the block.
 *
 * @param location The location to be centered.
 * @return The centered location.
 */
fun centerLocation(location: Location): Location {
    val loc = location.clone()
    loc.add(0.5, 0.5, 0.5)
    loc.yaw = 180f
    loc.pitch = 0f
    return loc
}

/**
 * Get a bukkit color from a hex code
 *
 * @param hex The hex code
 * @return The bukkit color
 */
fun fromHex(hex: String): Color {
    val color: java.awt.Color = try {
        java.awt.Color.decode(hex)
    } catch (ex: NumberFormatException) {
        return Color.BLACK
    }

    return Color.fromRGB(color.red, color.green, color.blue)
}

fun applyPapi(player: OfflinePlayer? = null, msg: String, stringPlaceholders: StringPlaceholders = StringPlaceholders.empty()): String {
    if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
        return msg

    return PlaceholderAPI.setPlaceholders(player, stringPlaceholders.apply(msg))
}