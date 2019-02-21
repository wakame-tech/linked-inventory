package tech.wakame.linkedinventory

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import tech.wakame.linkedinventory.Main.LIConfig
import java.util.NoSuchElementException
import kotlin.math.min

/**
 * A group of Linked-inventory command's handlers
 */
object Commander {
  /**
   * A map of command to handler
   */
  private val command: MutableMap<String, (CommandSender, Array<out String>) -> Boolean> = mutableMapOf()

  /**
   * available commands
   */
  val commands = command.keys

  init {
    // register handlers
    command["set"] = { sender, args -> set(sender, args) }
    command["tp"] = { sender, args -> tp(sender, args) }
    command["open"] = { sender, args -> open(sender, args) }
  }

  /**
   * register a location where the player is to [LIConfig.locations].
   * @return result of command
   */
  private fun set (sender: CommandSender, args: Array<out String>): Boolean {
    if (sender !is Player) return false
    LIConfig.locations[args.first()] = sender.location
    sender.sendMessage("${sender.location.inspect()} has been set as ${args.first()}")
    return true
  }

  /**
   * register a location where the player is to [LIConfig.locations].
   * @return result of command
   */
  private fun tp (sender: CommandSender, args: Array<out String>):Boolean {
    if (sender !is Player) return false
    // tp: listing spots
    if (args.isEmpty()) {
      LIConfig.locations.forEach { k, v -> sender.sendMessage("$k: ${v.inspect()}") }
    } else {
      LIConfig.locations[args.first()]?.let {
        sender.teleport(it)
      }
    }
    return true
  }

  /**
   * list up links as inventory view which can access inventory
   */
  private fun open(sender: CommandSender, args: Array<out String>): Boolean {
    if (sender !is Player) return false
    /*
     command options
       -i <item> : filtering links which include <item>.
    */
    sender.sendMessage(args)
    val options = args.iterator()
    val filters = mutableListOf<(List<ItemStack?>) -> Boolean>()
    while (options.hasNext()) {
      when (options.next()) {
        "-i" -> {
          try {
            val query = options.next().toUpperCase()
            filters.add { it.filterNotNull().any { query in it.type.toString() } }
          } catch (e: NoSuchElementException) {
            sender.sendMessage("[/open] -i option needs string")
          }
        }
      }
    }

    val linkedChestsIUI = Bukkit.createInventory(null, 54, "Linked Chests").also {
      LIConfig.linkedChestLocations.toList()
        .mapNotNull { (key, location) ->
          // convert to inventory
          if (location.block.state is InventoryHolder) {
            key to (location.block.state as InventoryHolder).inventory
          } else {
            null
          }
        }
        .filter { (_, inventory) ->
          // apply filters
          val filterResults = filters.map { filter -> filter(inventory.toList()) }
          println(filterResults)
          filterResults.fold(true) { acc, cond -> acc && cond }
        }
        .take(54)
        .forEachIndexed { index, (k, inventory) ->
        // set icon
        it.setItem(index, ItemStack(Material.CHEST, 1).also { icon ->
          val im = icon.itemMeta
          im.displayName = k
          val innerItems = inventory.toList().filterNotNull()
          im.lore = innerItems
            .take(5)
            .map { "${it.type.name} x${it.amount}" }
          icon.itemMeta = im
        })
      }
    }
    sender.openInventory(linkedChestsIUI)
    return true
  }

  fun dispatch(sender: CommandSender?, label: String?, args: Array<out String>?): Boolean {
    return if (label in commands && sender != null && args != null) {
      command[label]!!.invoke(sender, args)
    } else {
      false
    }
  }
}