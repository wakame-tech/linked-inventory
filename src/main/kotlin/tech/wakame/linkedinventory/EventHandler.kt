package tech.wakame.linkedinventory

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import java.util.*

object EventHandler {
  fun mineAll(target: Block, tool: ItemStack, dest: Location) {
    if (
      !(tool.type in Constants.Tools.PICKAXES && target.type in Constants.Blocks.ALL_MINING) &&
      !(tool.type in Constants.Tools.AXES && target.type in Constants.Blocks.ALL_CUTTING)
    ) return

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
        val nextTarget = target.getRelative(it, 1)
        if (nextTarget.type == target.type) {
          blockBreak(nextTarget)
        }
      }
    }

    blockBreak(target)
  }
}