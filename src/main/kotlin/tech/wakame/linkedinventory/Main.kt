package tech.wakame.linkedinventory

import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.Configuration
import org.bukkit.plugin.java.JavaPlugin


class Main : JavaPlugin() {
  object LIConfig {
    /**
     *
     */
    lateinit var config: Configuration
    /**
     *
     */
    var locations: MutableMap<String, Location> = mutableMapOf()
    /**
     *
     */
    var linkedChestLocations: MutableMap<String, Location> = mutableMapOf()
    /**
     *
     */
    var clipBoard: Array<Location?> = arrayOf(null, null)

    /**
     *
     */
    fun load(config: Configuration) {
      this.config = config
      locations = getElements("locations")
      linkedChestLocations = getElements("chests")
    }

    /**
     *
     */
    fun save() {
      setElements("locations", locations)
      setElements("chests", linkedChestLocations)
    }

    /**
     *
     */
    private inline fun <reified V : Any> getElements (path: String): MutableMap<String, V> {
      config.getConfigurationSection(path)?.let { section ->
        return section.getKeys(false)
          .combineNotNull { config["$path.$it"] as? V }
          .toMutableMap()
      }
      return mutableMapOf()
    }

    /**
     *
     */
    private fun <V> setElements (path: String, data: MutableMap<String, V>) {
      config.createSection(path)
      data.forEach { k, v -> config.set("$path.$k", v) }
    }
  }

  /**
   *
   */
  override fun onEnable() {
    logger.info("[LinkedInventory 0.3 alpha]")
    server.pluginManager.registerEvents(EventHandler, this)
    LIConfig.load(this.config)
  }

  /**
   *
   */
  override fun onDisable() {
    LIConfig.save()
    saveConfig()
  }

  /**
   *
   */
  override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
    return if (label in Commander.commands) {
      Commander.dispatch(sender, label, args)
    } else {
      super.onCommand(sender, command, label, args)
    }
  }
}
