package tech.wakame.linkedinventory

import org.bukkit.Material
import org.bukkit.block.BlockFace

object Constants {
  val DIRECTIONS = arrayOf(
    BlockFace.DOWN, BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST
  )

  object Tools {
    val PICKAXES = arrayOf(
      Material.DIAMOND_PICKAXE,
      Material.IRON_PICKAXE,
      Material.GOLDEN_PICKAXE,
      Material.STONE_PICKAXE,
      Material.WOODEN_PICKAXE
    )
    val AXES = arrayOf(
      Material.DIAMOND_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.STONE_AXE, Material.WOODEN_AXE
    )
    val SHOVELS = arrayOf(
      Material.DIAMOND_SHOVEL,
      Material.IRON_SHOVEL,
      Material.GOLDEN_SHOVEL,
      Material.STONE_SHOVEL,
      Material.WOODEN_SHOVEL
    )
  }

  object Blocks {
    val ALL_MINING = arrayOf(
      Material.GLOWSTONE,
      Material.COAL_ORE,
      Material.IRON_ORE,
      Material.DIAMOND_ORE,
      Material.EMERALD_ORE,
      Material.GOLD_ORE,
      Material.LAPIS_ORE,
      Material.GLOWSTONE,
      Material.NETHER_QUARTZ_ORE,
      Material.REDSTONE_ORE,
      Material.OBSIDIAN
    )
    val ALL_CUTTING = arrayOf(
      Material.OAK_LOG,
      Material.SPRUCE_LOG,
      Material.ACACIA_LOG,
      Material.BIRCH_LOG,
      Material.DARK_OAK_LOG,
      Material.JUNGLE_LOG
    )
    val ALL_DIGGING = arrayOf(
      Material.GRAVEL,
      Material.SOUL_SAND,
      Material.CLAY
    )
  }
}