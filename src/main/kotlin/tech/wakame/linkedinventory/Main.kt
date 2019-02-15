package tech.wakame.linkedinventory

import fr.rhaz.minecraft.kotlin.bukkit.*
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.plugin.java.JavaPlugin


class Main : JavaPlugin() {
  override fun onEnable() {
    // MineAll, CutAll
    listen<BlockBreakEvent> {
      EventHandler.mineAll(it.block, it.player.inventory.itemInMainHand, it.player.location)
    }
  }

  override fun onDisable() {

  }
}
