package tech.wakame.linkedinventory

import org.bukkit.plugin.java.JavaPlugin
import fr.rhaz.minecraft.kotlin.bukkit.*

class Main : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        val a = listOf<Int>(1, 2, 3)
        logger.info("Hello Spigot! ${a}")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
