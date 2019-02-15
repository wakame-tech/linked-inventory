package tech.wakame.linkedinventory

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import java.util.*

object EventHandler : Listener {
  @EventHandler
  fun onBlockBreak(event: BlockBreakEvent) {
    mineAll(event.block, event.player.inventory.itemInMainHand, event.player.location)
  }

  @EventHandler
  fun onPlayerJoin(event: PlayerJoinEvent) {
    event.player.sendMessage("a")
  }

  private fun mineAll(target: Block, tool: ItemStack, dest: Location) {
    if (
      !(tool.type in Constants.Tools.PICKAXES && target.type in Constants.Blocks.ALL_MINING) &&
      !(tool.type in Constants.Tools.AXES && target.type in Constants.Blocks.ALL_CUTTING) &&
      !(tool.type in Constants.Tools.SHOVELS && target.type in Constants.Blocks.ALL_DIGGING)
    ) return

    var limit = 100
    fun blockBreak(target: Block) {
      when {
        Enchantment.LOOT_BONUS_BLOCKS in tool.enchantments.keys -> {
          val amount = Math.max(0, Random().nextInt(tool.enchantments[Enchantment.LUCK]!! + 2) - 1)
          listOf(ItemStack(target.drops.first().type, amount))
        }
        Enchantment.SILK_TOUCH in tool.enchantments.keys -> {
          listOf(ItemStack(target.type, 1))
        }
        else -> {
          target.drops
        }
      }.forEach {
        dest.world.dropItemNaturally(dest, it)
      }

      target.type = Material.AIR

      Constants.DIRECTIONS.forEach {
        val nextTarget = target.getRelative(it)
        if (nextTarget.type == target.type && 0 < limit) {
          limit--
          blockBreak(nextTarget)
        }
      }
    }
    blockBreak(target)
  }
}