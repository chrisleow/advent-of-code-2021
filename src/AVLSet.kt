/**
 * Uses an AVLTree as a map
 */
class AVLSet<T: Comparable<T>> private constructor(private val tree: AVLTree<T>) : Set<T> {
    override val size: Int = tree.size
    override fun contains(element: T) = tree.contains(element)
    override fun containsAll(elements: Collection<T>) = elements.all { this.contains(it) }
    override fun isEmpty() = (tree.size == 0)
    override fun iterator() = tree.scan().iterator()

    // unique ordered interface
    fun scan() = tree.scan()
    fun scanFrom(from: T, inclusive: Boolean = true) = tree.scanFrom(from, inclusive)
    fun scanReversed() = tree.scanReversed()
    fun scanFromReversed(from: T, inclusive: Boolean = true) = tree.scanFromReversed(from, inclusive)

    // modifiers
    operator fun plus(element: T) = AVLSet(tree.add(element))
    operator fun plus(elements: Iterable<T>) = AVLSet(tree.addAll(elements))
    operator fun minus(element: T) = AVLSet(tree.remove(element))
    operator fun minus(elements: Iterable<T>) = AVLSet(tree.removeAll(elements))

    companion object {
        operator fun <T: Comparable<T>> invoke(elements: Iterable<T>) =
            AVLSet(AVLTree<T>(compareBy { it }).addAll(elements))
    }
}

fun main() {
    val endSet = AVLSet(listOf(1, 7, 5, 4, 3, 2, 1)) + 8 + -1
    println(endSet.scan().toList())
}