package tech.wakame.linkedinventory

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import tech.wakame.linkedinventory.Main.LIConfig

object Commander {
  private val command: MutableMap<String, (CommandSender, Array<out String>) -> Boolean> = mutableMapOf()

  val commands = command.keys

  init {
    command["set"] = { sender, args -> set(sender, args) }
    command["tp"] = { sender, args -> tp(sender, args) }
  }

  private fun set (sender: CommandSender, args: Array<out String>): Boolean {
    if (sender !is Player) return false
    LIConfig.locations[args.first()] = sender.location
    sender.sendMessage("${sender.location.inspect()} has been set as ${args.first()}")
    return true
  }

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

  fun dispatch(sender: CommandSender?, label: String?, args: Array<out String>?): Boolean {
    return if (label in commands && sender != null && args != null) {
      command[label]!!.invoke(sender, args)
    } else {
      false
    }
  }
}