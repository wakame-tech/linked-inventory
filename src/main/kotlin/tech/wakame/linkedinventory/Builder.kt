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
   *  Assist to build a framework of building
   */
  class FrameWork(val options: Map<String, String?>, val region: Pair<Location, Location>) {
    private var foundationVertices: List<Location>

    init {
      println(options)
      foundationVertices = detectEdges()
      connectEdges(foundationVertices)
    }

    /**
     *  regard a block placed 1-block higher than the ground level as a vertex
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

      println("[/build fw] groundLevel: $groundLevel")

      // detect as vertex locates on the ground level + 1
      val vertices = mutableListOf<Location>()
      region.horizontallyForEach(groundLevel) {
        if (!it.block.isEmpty && it.block.type != Material.TORCH) {
          vertices.add(it)
        }
      }

      return vertices.toList()
    }

    /**
     *  calculate vertices' pseido concave hull and connect vertices
     */
    private fun connectEdges(vertices: List<Location>) {
      val type = vertices.first().block.type
      val y = vertices.first().blockY
      val world = vertices.first().world

      val edges: List<Point> = vertices.map { Point(it.blockX, it.blockZ) }

      try {
        val concaveEdges = PseidoConcaveHull.Iterator(edges).toList()
        concaveEdges
          .map {
            Location(world, it.x.toDouble(), y.toDouble(), it.y.toDouble())
          }
          .windowed(2) { (c, n) ->
            //          println("${c.inspect()} -> ${n.inspect()}")
            (c to n).horizontallyForEach(y) {
              it.block.type = type
            }
          }
      } catch (e: NoSuchElementException) {
        println("[/build fw] failed to connect edges.")
        return
      }
    }


  }
}