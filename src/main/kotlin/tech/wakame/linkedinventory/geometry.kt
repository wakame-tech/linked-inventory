package tech.wakame.linkedinventory

import com.sun.org.apache.xpath.internal.operations.Bool
import java.util.*
import kotlin.math.round
import kotlin.math.roundToInt

/**
 *  see <https://github.com/bmaslakov/kotlin-algorithm-club/blob/master/src/main/io/uuddlrlrba/ktalgs/geometry/Point.kt>
 */
data class Point(val x: Int, val y: Int): Comparable<Point> {
  override fun compareTo(other: Point): Int {
    if (x == other.x) return y.compareTo(other.y)
    return x.compareTo(other.x)
  }

  fun distanceSq(to: Point): Int {
    return (x - to.x) * (x - to.x) + (y - to.y) * (y - to.y)
  }

  fun isLeftOfLine(from: Point, to: Point): Boolean {
    return crossProduct(from, to) > 0
  }

  fun crossProduct(origin: Point, p2: Point): Int {
    return (p2.x - origin.x) * (this.y - origin.y) - (p2.y - origin.y) * (this.x - origin.x)
  }

  fun distanceToLine(a: Point, b: Point): Double {
    return Math.abs((b.x - a.x) * (a.y - this.y) - (a.x - this.x) * (b.y - a.y)) /
            Math.sqrt(Math.pow((b.x - a.x).toDouble(), 2.0) + Math.pow((b.y - a.y).toDouble(), 2.0))
  }

  fun euclideanDistanceTo(that: Point): Double {
    return EUCLIDEAN_DISTANCE_FUNC(this, that)
  }

  fun manhattanDistanceTo(that: Point): Double {
    return MANHATTAN_DISTANCE_FUNC(this, that)
  }

  companion object {
    // < 0 : Counterclockwise
    // = 0 : p, q and r are colinear
    // > 0 : Clockwise
    fun orientation(p: Point, q: Point, r: Point): Int {
      return (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y)
    }

    val EUCLIDEAN_DISTANCE_FUNC: (Point, Point) -> (Double) = { p, q ->
      val dx = p.x - q.x
      val dy = p.y - q.y
      Math.sqrt((dx * dx + dy * dy).toDouble())
    }

    val MANHATTAN_DISTANCE_FUNC: (Point, Point) -> (Double) = { p, q ->
      val dx = p.x - q.x
      val dy = p.y - q.y
      Math.sqrt((dx * dx + dy * dy).toDouble())
    }
  }
}

/**
 *  return next point with pseido-concave-hull on plane lattice
 *
 *  TODO find bugs in specific shape
 */
class PseidoConcaveHull(val points: List<Point>) {
  /**
   *  see <https://github.com/bmaslakov/kotlin-algorithm-club/blob/master/src/main/io/uuddlrlrba/ktalgs/geometry/convexhull/Quickhull.kt>
   */
  private fun convexHull(points: List<Point>): List<Point> {
    if (points.size < 3) throw IllegalArgumentException("there must be at least 3 points")
    val left = points.min()!!
    val right = points.max()!!
    return quickHull(points, left, right) + quickHull(points, right, left)
  }

  private fun quickHull(points: List<Point>, first: Point, second: Point): List<Point> {
    val pointsLeftOfLine = points
      .filter { it.isLeftOfLine(first, second) }
      .map { Pair(it, it.distanceToLine(first, second)) }
    if (pointsLeftOfLine.isEmpty()) {
      return listOf(second)
    } else {
      val max = pointsLeftOfLine.maxBy { it.second }!!.first
      val newPoints = pointsLeftOfLine.map { it.first }
      return quickHull(newPoints, first, max) + quickHull(newPoints, max, second)
    }
  }

  // checks that express iterator unreached
  private val unreached: MutableSet<Point> = mutableSetOf()
  // center point of convex hull
  private var center: Point
  // start
  private var start: Point
  // current point
  private var current: Point
  // count == points.size + 1 means start visited twice
  private var count: Int = 0

  init {
    unreached.addAll(points)
    val edges = convexHull(points)
    center = Point(edges.map { it.x }.average().roundToInt(), edges.map { it.y }.average().roundToInt())
    start = unreached.first()
    current = start
  }

  operator fun hasNext(): Boolean = unreached.isNotEmpty()

  operator fun next(): Point {
    count++
    // find next near candidate meets same x or y
    // 4 directions
    val candidates = arrayOf(
      points.filter { it.x < current.x && it.y == current.y }.minBy { it.distanceSq(current) },
      points.filter { it.y < current.y && it.x == current.x }.minBy { it.distanceSq(current) },
      points.filter { it.x > current.x && it.y == current.y }.minBy { it.distanceSq(current) },
      points.filter { it.y > current.y && it.x == current.x }.minBy { it.distanceSq(current) }
    )
      .filterNotNull()
      .filter { it in unreached }

    val candidate = candidates
      .maxBy { it.distanceSq(center) }
    ?: throw NoSuchElementException()

    println("current: $current next: $candidate")

    unreached.remove(candidate)

    current = candidate
    return candidate
  }

  class Iterator(private val points: List<Point>) {
    operator fun iterator() = PseidoConcaveHull(points)

    fun toList(): List<Point> {
      val list = mutableListOf<Point>()
      for (p in this) {
        list.add(p)
      }
      list.add(list.first())
      return list.toList()
    }
  }
}