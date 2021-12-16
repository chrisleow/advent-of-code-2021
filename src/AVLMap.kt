/**
 * Uses an AVLTree as a map
 */
class AVLMap<K: Comparable<K>, V: Any> private constructor(private val tree: AVLTree<Pair<K, V?>>) : Map<K, V> {

    data class AVLMapEntry<K, V>(override val key: K, override val value: V) : Map.Entry<K, V>

    private fun Sequence<Pair<K, V?>>.toMapEntries() = this.mapNotNull { (k, v) -> v?.let { AVLMapEntry(k, it) } }

    // modification operations
    operator fun plus(entry: Pair<K, V>) = AVLMap(tree.add(entry))
    operator fun plus(entry: Map.Entry<K, V>) = AVLMap(tree.add(Pair(entry.key, entry.value)))
    operator fun plus(map: Map<K, V>) = AVLMap(tree.addAll(map.map { Pair(it.key, it.value) }))
    operator fun plus(entries: Iterable<Pair<K, V>>) = AVLMap(tree.addAll(entries))
    operator fun minus(key: K) = AVLMap(tree.remove(Pair(key, null)))
    operator fun minus(keys: Iterable<K>) = AVLMap(tree.removeAll(keys.map { Pair(it, null) }))

    fun scan() = tree.scan().toMapEntries()
    fun scanFrom(from: K, inclusive: Boolean = true) = tree.scanFrom(Pair(from, null), inclusive).toMapEntries()
    fun scanReversed() = tree.scanReversed().toMapEntries()
    fun scanFromReversed(from: K, inclusive: Boolean = true) =
        tree.scanFromReversed(Pair(from, null), inclusive).toMapEntries()

    override val size = tree.size
    override operator fun get(key: K): V? = tree.find(Pair(key, null))?.second

    override val entries: Set<Map.Entry<K, V>> = object : Set<Map.Entry<K, V>> {
        override val size = tree.size
        override fun contains(element: Map.Entry<K, V>): Boolean {
            val entry = tree.find(Pair(element.key, null))
            return when {
                entry == null -> false
                entry.first == element.key && entry.second == element.value -> true
                else -> false
            }
        }
        override fun containsAll(elements: Collection<Map.Entry<K, V>>) = elements.all { this.contains(it) }
        override fun isEmpty() = (tree.size == 0)
        override fun iterator() = tree.scan().toMapEntries().iterator()
    }

    override val keys: Set<K> = object : Set<K> {
        override val size = tree.size
        override fun contains(element: K) = tree.contains(Pair(element, null))
        override fun containsAll(elements: Collection<K>) = elements.all { tree.contains(Pair(it, null)) }
        override fun isEmpty() = (tree.size == 0)
        override fun iterator() = tree.scan().map { it.first }.iterator()
    }

    override val values: Collection<V> = object : AbstractCollection<V>() {
        override val size = tree.size
        override fun iterator() = tree.scan().mapNotNull { it.second }.iterator()
    }

    override fun containsKey(key: K): Boolean = tree.contains(Pair(key, null))
    override fun containsValue(value: V): Boolean = tree.scan().any { it.second == value }
    override fun isEmpty(): Boolean = (tree.size == 0)

    companion object {
        operator fun <K: Comparable<K>, V: Any> invoke() = AVLMap(AVLTree<Pair<K, V?>>(compareBy { it.first }))
    }
}

fun main() {
    val map = AVLMap<Int, String>()
    val endMap = map.plus(Pair(1, "Howdy!")).plus(Pair(3, "Cromity")).plus(Pair(2, "Bingo!"))
    println(endMap.scan().toList())
}