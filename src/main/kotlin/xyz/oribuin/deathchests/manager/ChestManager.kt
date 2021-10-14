package xyz.oribuin.deathchests.manager

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import xyz.oribuin.deathchests.EternalDeathChests
import xyz.oribuin.deathchests.chest.DeathChest
import xyz.oribuin.deathchests.util.getBlockLoc
import xyz.oribuin.gui.Item
import xyz.oribuin.orilibrary.manager.Manager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*


class ChestManager(private val plugin: EternalDeathChests) : Manager(plugin) {

    val cachedChests = mutableMapOf<Location, DeathChest>()

    // here comes the excessive namespace keys
    private val owner = NamespacedKey(plugin, "player")
    private val spawnTime = NamespacedKey(plugin, "spawnTime")
    private val items = NamespacedKey(plugin, "items")
    private val xp = NamespacedKey(plugin, "xp")
    private val holoLoc = NamespacedKey(plugin, "hololoc")

    /**
     * Get a death chest from an armorstand
     *
     * @param stand The armorstand which would likely be in the ground.
     * @return An optional death chest if it exists.
     */
    fun getChest(stand: ArmorStand): Optional<DeathChest> {
        val cont = stand.persistentDataContainer

        // Check if it is a death chest first.
        val ownerUUID = cont.get(owner, PersistentDataType.STRING) ?: return Optional.empty()
        val owner = UUID.fromString(ownerUUID)
        val items = cont.get(items, PersistentDataType.BYTE_ARRAY) ?: return Optional.empty()
        val spawnTime = cont.get(spawnTime, PersistentDataType.LONG) ?: System.currentTimeMillis()
        val xp = cont.get(xp, PersistentDataType.INTEGER) ?: 0
        val holoLoc = cont.get(holoLoc, PersistentDataType.STRING)

        val chest = DeathChest(plugin, owner, getBlockLoc(stand.location))
        chest.armorStand = stand
        chest.deathItems = decompressItemStacks(items).toMutableList()
        chest.spawnTime = spawnTime
        chest.savedXP = xp
        chest.holoLocation = deserializeLocation(holoLoc)
        chest.createOrUpdateHolo()

        return Optional.of(chest)
    }

    /**
     * Check if the armorstand is a deathchest
     *
     * @param stand The armorstand.
     * @return true if is deathchest.
     */
    fun isChest(stand: ArmorStand): Boolean {
        return stand.persistentDataContainer.has(owner, PersistentDataType.STRING)
    }

    /**
     * Create an armorstand at a location.
     *
     * @param player The player if there is a texture;
     * @param loc    The location of the armorstand.
     */
    fun makeArmorstand(player: Player, loc: Location, chest: DeathChest): ArmorStand {
        val headTexture = plugin.config.getString("deathchest-settings.head-texture")
        val builder = Item.Builder(Material.PLAYER_HEAD)

        // Add a texture of the player to the head
        if (headTexture != null && headTexture.equals("%player_name%", ignoreCase = true)) {
            builder.setOwner(player)
        } else {
            builder.setTexture(headTexture)
        }

        return loc.world!!.spawn(loc.clone(), ArmorStand::class.java) { x ->
            x.isVisible = true
            x.setGravity(false)
            x.isInvulnerable = true
            x.equipment?.helmet = builder.create()

            // Make the armorstand unchangeable
            Arrays.stream(EquipmentSlot.values()).forEach {
                x.addEquipmentLock(it, ArmorStand.LockType.ADDING_OR_CHANGING)
                x.addEquipmentLock(it, ArmorStand.LockType.REMOVING_OR_CHANGING)
            }

            chest.update(x)
        }
    }

    fun compressItemStacks(items: List<ItemStack>): ByteArray {
        var data = ByteArray(0)
        try {
            ByteArrayOutputStream().use { os ->
                BukkitObjectOutputStream(os).use { oos ->
                    oos.writeInt(items.size)
                    for (itemStack in items) oos.writeObject(itemStack)
                    data = os.toByteArray()
                }
            }
        } catch (ignored: IOException) {
        }
        return data
    }

    private fun decompressItemStacks(data: ByteArray): List<ItemStack> {
        val items: MutableList<ItemStack> = ArrayList()
        try {
            ByteArrayInputStream(data).use { `is` ->
                BukkitObjectInputStream(`is`).use { ois ->
                    val amount = ois.readInt()
                    for (i in 0 until amount) items.add(ois.readObject() as ItemStack)
                }
            }
        } catch (ignored: IOException) {
        } catch (ignored: ClassNotFoundException) {
        }
        return items
    }

    /**
     * Serialize a location into a base64 value.
     *
     * @param location The location.
     * @return The serialized location.
     */
    fun serializeLocation(location: Location): String {
        val config = YamlConfiguration()
        config["location"] = location
        return Base64.getEncoder().encodeToString(config.saveToString().toByteArray())
    }

    /**
     * Deserialize a Base64 Value into a Location
     *
     * @param serialized The serialized base64 value
     * @return The deserialized location.
     */
    private fun deserializeLocation(serialized: String?): Location? {
        serialized ?: return null

        val config = YamlConfiguration()
        try {
            config.loadFromString(String(Base64.getDecoder().decode(serialized)))
        } catch (e: InvalidConfigurationException) {
            e.printStackTrace()
        }

        return config.getLocation("location")
    }

    private fun holo(extra: String): NamespacedKey {
        return NamespacedKey(plugin, "hololoc-$extra")
    }

}