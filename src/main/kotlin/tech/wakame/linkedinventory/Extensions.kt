package tech.wakame.linkedinventory

import org.bukkit.Location

inline fun <K, V : Any> Collection<K>.combineNotNull(crossinline transform: (K) -> V?): Map<K, V> {
  return this.mapNotNull {
    val v = transform(it)
    return@mapNotNull if (v is V) {
      Pair(it, v)
    } else {
      null
    }
  }.toMap()
}

fun Location.inspect() = "(${x.toInt()}, ${y.toInt()}, ${z.toInt()})"