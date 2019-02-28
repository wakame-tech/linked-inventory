package tech.wakame.linkedinventory

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import tech.wakame.linkedinventory.Main.LIConfig
import java.io.File
import java.util.*

/**
 * A group of Linked-inventory command's handlers
 */
object Commander {
  /**
   * A map of command to handler
   */
  private val command: MutableMap<String, (CommandSender, Array<String>, Map<String, String?>) -> Boolean> = mutableMapOf()

  /**
   * available commands
   */
  val commands = command.keys

  init {
    // register handlers
    command["set"] = { sender, params, options -> set(sender, params, options) }
    command["tp"] = { sender, params, options -> tp(sender, params, options) }
    command["open"] = { sender, params, options -> open(sender, params, options) }
    command["build"] = { sender, params, options -> build(sender, params, options) }
    command["cave"] = { sender, params, options -> cave(sender, params, options) }
  }

  /**
   * register a location where the player is to [LIConfig.locations].
   * @return result of command
   */
  private fun set (sender: CommandSender, params: Array<String>, options: Map<String, String?>): Boolean {
    if (sender !is Player) return false
    LIConfig.locations[params.first()] = sender.location
    sender.sendMessage("${sender.location.inspect()} has been set as ${params.first()}")
    return true
  }

  /**
   * register a location where the player is to [LIConfig.locations].
   * @return result of command
   */
  private fun tp (sender: CommandSender, params: Array<String>, options: Map<String, String?>):Boolean {
    if (sender !is Player) return false
    // tp: listing spots
    if (params.isEmpty()) {
      LIConfig.locations.forEach { k, v -> sender.sendMessage("$k: ${v.inspect()}") }
    } else {
      LIConfig.locations[params.first()]?.let {
        sender.teleport(it)
      }
    }
    return true
  }

  /**
   * list up links as inventory view which can access inventory
   */
  private fun open(sender: CommandSender, params: Array<String>, options: Map<String, String?>): Boolean {
    if (sender !is Player) return false

    val filters = mutableListOf<(Inventory) -> Boolean>()

    /*
     command options
       -i <item> : filtering links which include <item>.
       -n <spot> : list up links which close to <spot>
    */
    options["-i"]?.let {
      if (options["-i"] == null) {
        sender.sendMessage("[/open] -i option needs string")
        return@let
      }
      val query = it.toUpperCase()
      filters.add { inventory ->
        inventory.toList().filterNotNull().any { itemStack -> query in itemStack.type.toString() }
      }
    }

    options["-n"]?.let {
      if (options["-n"] == null) {
        sender.sendMessage("[/open] -n option needs spot name")
        return@let
      }
      val center = LIConfig.locations[it] ?: sender.location
      filters.add { inventory -> inventory.location.distance(center) < 50.0 }
    }

    // create result view
    val linkedChestsIUI = Bukkit.createInventory(null, 54, "Linked Chests").also {
      LIConfig.linkedChestLocations.toList()
        .mapNotNull { (key, location) -> // convert to inventory
          if (location.block.state is InventoryHolder) {
            key to (location.block.state as InventoryHolder).inventory
          } else {
            null
          }
        }
        .filter { (_, inventory) -> // apply filters
          val filterResults = filters.map { filter -> filter(inventory) }
          filterResults.fold(true) { acc, cond -> acc && cond }
        }
        .take(54)
        .forEachIndexed { index, (k, inventory) -> // set icon
          val innerItemsInfo = inventory.toList()
            .filterNotNull()
            .groupBy { it.type }
            .map { (m, iss) -> m to iss.map { it.amount }.sum() }
            .sortedByDescending { it.second }
            .take(10)
            .map { "${it.first} x${it.second}" }
          val icon = Material.CHEST.createIcon(k, innerItemsInfo)
          it.setItem(index, icon)
        }
    }
    sender.openInventory(linkedChestsIUI)
    return true
  }

  /**
   *
   */
  private fun build(sender: CommandSender, params: Array<String>, options: Map<String, String?>): Boolean {
    /*
    * [Usage]
    *  /build <type> [options ...]
    *
    *  type:
    *   fw : frame work
    *
    *   options:
    *    --height <int> :
    *    --story <int> :
    *
    */
    if (params.size != 1) {
      sender.sendMessage("[/build] please set type. type = \"fw\"")
      return false
    }

    return when (params.first()) {
      "fw" -> {
        val from = LIConfig.clipBoard[0]
        val to = LIConfig.clipBoard[1]
        if (from == null || to == null) {
          sender.sendMessage("[/build fw] require 2 clipboarded locations")
          return false
        }

        Builder.FrameWork(options, from to to)
        true
      }
      else -> {
        sender.sendMessage("[/build] unknown type. type = \"fw\"")
        false
      }
    }
  }

  /**
   *
   */
  private fun cave(sender: CommandSender, params: Array<String>, options: Map<String, String?>): Boolean {
    if (sender !is Player) return false

    val LIMIT = 100000
    var count = 0

    fun locationDecorator(l: Location) = Triple(l.blockX, l.blockY, l.blockZ)

    fun bfs(start: Location) {
      val history = mutableSetOf<Triple<Int, Int, Int>>()
      val queue = LinkedList<Location>()
      queue.push(start)
      history.add(locationDecorator(start))
      var loc = start
      while (queue.isNotEmpty()) {
        loc = queue.pop()
        if (LIMIT < count++) break
        Constants.DIRECTIONS.forEach {
          val next = loc.block.getRelative(it, 1)
          if (next.isEmpty && next.location.blockY < 63 && locationDecorator(next.location) !in history) {
            history.add(locationDecorator(next.location))
            queue.push(next.location)
          }
        }
      }

      // filter location
      val floors = history.filter { it.copy(second = it.second - 1) !in history }

      // write plots
      val src = File("plot.xyz")
      sender.sendMessage("plots saved at ${src.absolutePath}")
      src.absoluteFile.writeText(floors.map { (x, y, z) -> "$x $y $z" }.joinToString("\n"))
      sender.sendMessage("last location: ${loc.inspect()}")
    }

    bfs(sender.location)

    sender.sendMessage("[/cave] complete in $count iteration")

    return true
  }

  /**
   *  dispatch command from [Main.onCommand()].
   */
  fun dispatch(sender: CommandSender?, label: String?, args: Array<out String>?): Boolean {
    return if (label in commands && sender != null && args != null) {
      val (params, options) = args.toParamsAndOptions()
      command[label]!!.invoke(sender, params, options)
    } else {
      false
    }
  }
}