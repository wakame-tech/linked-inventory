package tech.wakame.linkedinventory

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.util.*
import tech.wakame.linkedinventory.Main.LIConfig

object EventHandler : Listener {
  /**
   *
   */
  @EventHandler
  fun onBlockBreak(event: BlockBreakEvent) {
    // region selection
    if (event.player.inventory.itemInMainHand.type == Material.STICK) {
      event.isCancelled = true
      // add to clipboard
      when {
        LIConfig.clipBoard[0] == null -> {
          LIConfig.clipBoard[0] = event.block.location
          event.player.sendMessage("[clipboard] first registered ${event.block.location.inspect()}")
        }
        // prevent from continuous selection
        LIConfig.clipBoard[1] == null && LIConfig.clipBoard[0] != event.block.location -> {
          LIConfig.clipBoard[1] = event.block.location
          event.player.sendMessage("[clipboard] second registered ${event.block.location.inspect()}")
        }
        LIConfig.clipBoard[1] != null -> {
          event.player.sendMessage("[clipboard] reset!")
          LIConfig.clipBoard = arrayOf(null, null)
        }
      }
      return
    }

    mineAll(event.block, event.player.inventory.itemInMainHand, event.player.location)
  }

  /**
   *
   */
  @EventHandler
  fun onPlayerJoin(event: PlayerJoinEvent) {

  }

  /**
   *
   */
  @EventHandler
  fun onInventoryOpen(event: InventoryOpenEvent) {
    if (event.inventory is DoubleChestInventory) {
      Main.LIConfig.linkedChestLocations[event.inventory.location.inspect()] = event.inventory.location
    }
  }

  /**
   *
   */
  @EventHandler
  fun onInventoryClick(event: InventoryClickEvent) {
    // when click out of inventory, [event.slot] will be -999.
    if (event.view.title == "Linked Chests") {
      event.isCancelled = true
      if (event.slot !in event.view.topInventory.toList().filterNotNull().indices) return
      val key = event.inventory.getItem(event.slot).itemMeta.displayName
      val loc = LIConfig.linkedChestLocations.toList().first { (k, v) -> key == k }.second
      val inv = (loc.block.state as? InventoryHolder)?.inventory
      event.whoClicked.openInventory(inv)
    }
  }

  /**
   *
   */
  private fun mineAll(firstTarget: Block, tool: ItemStack, dest: Location) {
    if (
      !(tool.type in Constants.Tools.PICKAXES && firstTarget.type in Constants.Blocks.ALL_MINING) &&
      !(tool.type in Constants.Tools.AXES && firstTarget.type in Constants.Blocks.ALL_CUTTING) &&
      !(tool.type in Constants.Tools.SHOVELS && firstTarget.type in Constants.Blocks.ALL_DIGGING)
    ) return

    var limit = 100
    val type = firstTarget.type

    fun blockBreak(target: Block) {
      if (limit < 0 || target.type == Material.AIR) return
      when {
        Enchantment.LOOT_BONUS_BLOCKS in tool.enchantments.keys -> {
          val amount = Math.max(1, Random().nextInt(tool.enchantments[Enchantment.LOOT_BONUS_BLOCKS]!! + 2) - 1)
          ItemStack(target.drops.first().type, amount)
        }
        Enchantment.SILK_TOUCH in tool.enchantments.keys -> {
          ItemStack(target.type, 1)
        }
        else -> {
          target.drops.first()
        }
      }.let {
        dest.world.dropItemNaturally(dest, it)
      }

      target.type = Material.AIR

      Constants.DIRECTIONS.forEach {
        val nextTarget = target.getRelative(it, 1)
        if (nextTarget.type == type) {
          limit--
          blockBreak(nextTarget)
        }
      }
    }
    blockBreak(firstTarget)
  }
}