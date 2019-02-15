package tech.wakame.linkedinventory

import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.Configuration
import org.bukkit.plugin.java.JavaPlugin


class Main : JavaPlugin() {
  object LIConfig {
    lateinit var config: Configuration
    var locations: MutableMap<String, Location> = mutableMapOf()

    fun load(config: Configuration) {
      this.config = config
      config.getConfigurationSection("locations")?.let { section ->
        locations = section.getKeys(false)
          .combineNotNull { config["locations.$it"] as? Location }
          .toMutableMap()
      }
    }

    fun save(config: Configuration) {
      config.createSection("locations")
      locations.forEach { k, v ->
        config.set("locations.$k", v)
      }
    }
  }

  override fun onEnable() {
    logger.info("[LinkedInventory 0.3 alpha]")
    server.pluginManager.registerEvents(EventHandler, this)
    LIConfig.load(this.config)
  }

  override fun onDisable() {
    LIConfig.save(config)
    saveConfig()
  }

  override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
    return if (label in Commander.commands) {
      Commander.dispatch(sender, label, args)
    } else {
      super.onCommand(sender, command, label, args)
    }
  }
}
