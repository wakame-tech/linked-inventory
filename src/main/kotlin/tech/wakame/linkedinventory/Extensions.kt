package tech.wakame.linkedinventory

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 *  make pair from key: K to not-null value: V that'll be let to transform
 */
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

/**
 * make custom itemstack
 *
 * @param name
 * @
 */
fun Material.createIcon(name: String, lore: List<String>): ItemStack {
  val icon = ItemStack(this, 1)
  val im = icon.itemMeta
  im.displayName = name
  im.lore = lore
  icon.itemMeta = im
  return icon
}

/**
 * string list classify params and options (starts with "-" or "--")
 *
 * @return params array and options map
 */
fun Array<out String>.toParamsAndOptions(): Pair<Array<String>, Map<String, String?>> {
  fun isOption(arg: String?) = arg != null && (arg.startsWith("-") || arg.startsWith("--"))

  val argsList = LinkedList<String>().also { it.addAll(this) }
  val params = mutableListOf<String>()
  val options = mutableMapOf<String, String?>()

  // regards args until starts options
  while (!isOption(argsList.peekFirst())) {
    params.add(argsList.pollFirst())
  }

  // rest of string list are options
  while (argsList.isNotEmpty()) {
    val opt = argsList.pollFirst()
    if (argsList.isNotEmpty()) {
      if (!isOption(argsList.peekFirst())) {
        // --opt val ... -> ["opt"] = "val"
        options[opt] = argsList.pollFirst()
      } else {
        // --opt --opt2 ... -> ["opt"] = null, ["opt2"] = ...
        options[opt] = null
      }
    } else {
      options[opt] = null
    }
  }
  return params.toTypedArray() to options.toMap()
}

/**
 *  [Location] to easy string, such as "(X, Y, Z)"
 */
fun Location.inspect() = "(${x.toInt()}, ${y.toInt()}, ${z.toInt()})"