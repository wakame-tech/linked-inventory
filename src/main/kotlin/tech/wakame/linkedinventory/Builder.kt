package tech.wakame.linkedinventory

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.util.BlockIterator
import org.bukkit.util.Vector

/**
 *
 */
class Builder {
  /**
   *
   */
  class FrameWork(val region: Pair<Location, Location>) {
    init {
      val edges = detectEdges()
      linkEdges(edges)
    }

    /**
     *
     */
    private fun detectEdges(): List<Location> {
      // set ground level
      val itr = BlockIterator(region.first.world, region.first.toVector(), Vector(0, 1, 0), 0.0,  256)
      var block = itr.next()
      while (itr.hasNext()) {
        block = itr.next()
        if (block.isEmpty) break
      }
      val groundLevel = block.location.blockY

      println("groundLevel: $groundLevel")

      // detect as vertex locates on the ground level + 1
      val vertices = mutableListOf<Location>()
      region.horizontallyForEach(groundLevel) {
        if (!it.block.isEmpty && it.block.type != Material.TORCH) {
          vertices.add(it)
        }
      }

      return vertices.toList()
    }

    private fun linkEdges(edges: List<Location>) {
      val type = edges.first().block.type
      val y = edges.first().blockY
      val world = edges.first().world
      val points: List<Point> = edges.map { Point(it.blockX, it.blockZ) }
      val concaveEdges = PseidoConcaveHull.Iterator(points).toList()
      concaveEdges
        .map {
          Location(world, it.x.toDouble(), y.toDouble(), it.y.toDouble())
        }
        .windowed(2) { (c, n) ->
          println("${c.inspect()} -> ${n.inspect()}")
          (c to n).horizontallyForEach(y) {
            it.block.type = type
          }
        }
    }
  }
}