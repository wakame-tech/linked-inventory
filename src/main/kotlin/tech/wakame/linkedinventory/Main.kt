package tech.wakame.linkedinventory

import fr.rhaz.minecraft.kotlin.bukkit.*
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.plugin.java.JavaPlugin


class Main : JavaPlugin() {
  object LIConfig : ConfigFile("config") {
    var locationsData by stringList("locations")
    val locations: MutableMap<String, Location> = mutableMapOf()

    override fun reload() {
      locationsData = locations.map { it.toString() }
    }
  }

  override fun onEnable() {
    saveDefaultConfig()
    // load config.yml
    init(LIConfig)

    LIConfig.locations["a"] = Location(server.worlds.first(), 0.0, 0.0, 0.0)

    info(LIConfig.locations.toString())

    // MineAll, CutAll
    listen<BlockBreakEvent> {
      EventHandler.mineAll(it.block, it.player.inventory.itemInMainHand, it.player.location)
    }

    // Locationer
    command("set") { sender, args ->
      if (sender !is Player) return@command
      LIConfig.locations[args.first()] = sender.location
      info("set ${sender.location} as ${args.first()}")
    }

    LIConfig.save()
    LIConfig.reload()
  }

  override fun onDisable() {
    LIConfig.save()

    saveConfig()
  }
}
