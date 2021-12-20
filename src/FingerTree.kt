interface Measure<V, T> {
    val empty: V
    fun combine(a: V, b: V): V
    operator fun invoke(item: T): V
}

fun main() {
    FingerTree.runTest()
}

/**
 * Monoid-Enhanced Finger Tres
 *
 * https://andrew.gibiansky.com/blog/haskell/finger-trees/
 */
class FingerTree<V: Any, T: Any> private constructor(private val root: Tree<V, T>) : Iterable<T> {

    /**
     * Public methods
     */

    override fun iterator(): Iterator<T> = root.toSequence().iterator()

    /**
     * Private Implementation
     */

    sealed class Tree<V : Any, T : Any> {
        abstract val measure: Measure<V, T>
        data class Empty<V : Any, T : Any>(
            override val measure: Measure<V, T>) : Tree<V, T>()
        data class Single<V : Any, T : Any>(
            val value: T,
            override val measure: Measure<V, T>) : Tree<V, T>()
        data class Deep<V : Any, T : Any>(
            val annotation: V,
            val left: Digit<T>,
            val middle: Tree<V, Branch<V, T>>,
            val right: Digit<T>,
            override val measure: Measure<V, T>) : Tree<V, T>()
    }

    sealed class Branch<V : Any, T : Any> {
        abstract val annotation: V

        data class Branch2<V : Any, T : Any>(
            override val annotation: V,
            val first: T,
            val second: T) : Branch<V, T>()
        data class Branch3<V : Any, T : Any>(
            override val annotation: V,
            val first: T,
            val second: T,
            val third: T) : Branch<V, T>()
    }

    sealed class Digit<T : Any> {
        data class One<T : Any>(val first: T) : Digit<T>()
        data class Two<T : Any>(val first: T, val second: T) : Digit<T>()
        data class Three<T : Any>(val first: T, val second: T, val third: T) : Digit<T>()
        data class Four<T : Any>(val first: T, val second: T, val third: T, val fourth: T) : Digit<T>()
    }

    // powers iterators
    private fun <T : Any> Tree<*, T>.toSequence(): Sequence<T> = when (this) {
        is Tree.Empty<*, T> -> emptySequence()
        is Tree.Single<*, T> -> sequenceOf(value)
        is Tree.Deep<*, T> -> sequence {
            yieldAll(left.toList())
            yieldAll(middle.toSequence().flatMap { it.toList() })
            yieldAll(right.toList())
        }
    }

    // helper functions for digits
    private fun <T : Any> Digit<T>.toList(): List<T> = when (this) {
        is Digit.One<T> -> listOf(first)
        is Digit.Two<T> -> listOf(first, second)
        is Digit.Three<T> -> listOf(first, second, third)
        is Digit.Four<T> -> listOf(first, second, third, fourth)
    }

    private fun <T : Any> List<T>.toDigit(): Digit<T> = when (this.size) {
        0 -> error("cannot transform an empty list to a digit")
        1 -> Digit.One(this[0])
        2 -> Digit.Two(this[0], this[1])
        3 -> Digit.Three(this[0], this[1], this[2])
        4 -> Digit.Four(this[0], this[1], this[2], this[3])
        else -> error("cannot transform a list of size ${this.size} into a digit.")
    }

    private fun <V : Any, T : Any> Branch<V, T>.toList(): List<T> = when (this) {
        is Branch.Branch2 -> listOf(first, second)
        is Branch.Branch3 -> listOf(first, second, third)
    }

    private fun <V : Any, T : Any> List<T>.toBranch(measure: Measure<V, T>): Branch<V, T> = when (this.size) {
        2 -> Branch.Branch2(measure(this), this[0], this[1])
        3 -> Branch.Branch3(measure(this), this[0], this[1], this[2])
        else -> error("cannot transform a list of size '${this.size}' to a branch.")
    }

    // helper function to convert any number of items (>= 2) into branches, globs everything into 2's, possibly
    // into 3's at the end
    private fun <V : Any, T : Any> List<T>.toBranches(measure: Measure<V, T>): List<Branch<V, T>> {
        tailrec fun consume(index: Int, buffer: List<T>, branches: List<Branch<V, T>>): List<Branch<V, T>> {
            return when {
                index >= this.size - 1 -> {
                    branches + buffer.toBranch(measure)
                }
                else -> when (buffer.size) {
                    3 -> {
                        val newBuffer= buffer.drop(2)
                        val newBranch = buffer.take(2).toBranch(measure)
                        consume(index + 1, newBuffer + this[index], branches + newBranch)
                    }
                    else -> {
                        consume(index + 1, buffer + this[index], branches)
                    }
                }
            }
        }
        return consume(0, emptyList(), emptyList())
    }

    // helper functions to deal with concatenating lists of monoids and measuring things
    private operator fun <V : Any, T : Any> Measure<V, T>.invoke(vararg items: T) = items
        .map { this(it) }
        .reduce { a, b -> combine(a, b) }
    private operator fun <V : Any, T : Any> Measure<V, T>.invoke(items: Iterable<T>) = items
        .map { this(it) }
        .reduce { a, b -> combine(a, b) }
    private operator fun <V : Any, T : Any> Measure<V, T>.invoke(digit: Digit<T>) = digit
        .toList()
        .map { this(it) }
        .reduce { a, b -> combine(a, b) }
    private operator fun <V : Any, T : Any> Measure<V, T>.invoke(branch: Branch<V, T>) = branch
        .toList()
        .map { this(it) }
        .reduce { a, b -> combine(a, b) }

    // lift measure to branch
    private fun <V : Any, T : Any> Measure<V, T>.toBranch(): Measure<V, Branch<V, T>> {
        val original = this
        return object : Measure<V, Branch<V, T>> {
            override val empty = original.empty
            override fun combine(a: V, b: V) = original.combine(a, b)
            override operator fun invoke(item: Branch<V, T>) = item.annotation
        }
    }

    // measure a tree
    private fun <V : Any, T : Any> Tree<V, T>.getAnnotation(): V = when (this) {
        is Tree.Empty<V, T> -> measure.empty
        is Tree.Single<V, T> -> measure(value)
        is Tree.Deep<V, T> -> annotation
    }

    // helper functions to rebuild a tree (with annotations) after replacing something
    private fun <V : Any, T : Any> Tree.Deep<V, T>.replace(
        left: List<T>? = null,
        middle: Tree<V, Branch<V, T>>? = null,
        right: List<T>? = null,
    ): Tree.Deep<V, T> {
        return Tree.Deep(
            annotation = measure.combine(
                (middle ?: this.middle).getAnnotation(),
                measure((left ?: this.left.toList()) + (right ?: this.right.toList())),
            ),
            left = left?.toDigit() ?: this.left,
            middle = middle ?: this.middle,
            right= right?.toDigit() ?: this.right,
            measure = measure,
        )
    }

    // helper function to create a tree from a list of items
    private fun <V : Any, T : Any> createTree(
        items: List<T>,
        middle: Tree<V, Branch<V, T>>,
        measure: Measure<V, T>
    ): Tree<V, T> {
        fun createDigits(): Pair<Digit<T>, Digit<T>> = when (items.size) {
            2 -> Pair(Digit.One(items[0]), Digit.One(items[1]))
            3 -> Pair(Digit.One(items[0]), Digit.Two(items[1], items[2]))
            4 -> Pair(Digit.Two(items[0], items[1]), Digit.Two(items[2], items[3]))
            else -> error("can't create digits for this number of elements")
        }
        return when (items.size) {
            0 -> Tree.Empty(measure)
            1 -> Tree.Single(items[0], measure)
            else -> createDigits().let { (left, right) ->
                Tree.Deep(
                    annotation = measure.combine(measure(left), measure(right)),
                    left = left,
                    middle = middle,
                    right = right,
                    measure = measure,
                )
            }
        }
    }

    private fun <V : Any, T : Any> createSimpleTree(items: List<T>, measure: Measure<V, T>): Tree<V, T> =
        createTree(items, Tree.Empty(measure.toBranch()), measure)

    // in simple cases can add to existing digts etc..., must create a new node for middle tree if we
    // run out of space
    fun <V : Any, T : Any> Tree<V, T>.prepend(item: T): Tree<V, T> = when (this) {
        is Tree.Empty<V, T> -> createSimpleTree(listOf(item), measure)
        is Tree.Single<V, T> -> createSimpleTree(listOf(item, value), measure)
        is Tree.Deep<V, T> -> {
            when (left) {
                is Digit.One<T> -> replace(left = listOf(item, left.first))
                is Digit.Two<T> -> replace(left = listOf(item, left.first, left.second))
                is Digit.Three<T> -> replace(left = listOf(item, left.first, left.second, left.third))
                is Digit.Four<T> -> replace(left = listOf(item, left.first),
                    middle = middle.prepend(left.toList().drop(1).toBranch(measure)))
            }
        }
    }

    // in simple cases can add to existing digts etc..., must create a new node for middle tree if we
    // run out of space
    fun <V: Any, T : Any> Tree<V, T>.append(item: T): Tree<V, T> = when (this) {
        is Tree.Empty<V, T> -> createSimpleTree(listOf(item), measure)
        is Tree.Single<V, T> -> createSimpleTree(listOf(value, item), measure)
        is Tree.Deep<V, T> -> {
            when (right) {
                is Digit.One<T> -> replace(right = listOf(right.first, item))
                is Digit.Two<T> -> replace(right = listOf(right.first, right.second, item))
                is Digit.Three<T> -> replace(right = listOf(right.first, right.second, right.third, item))
                is Digit.Four<T> -> replace(right = listOf(right.fourth, item),
                    middle = middle.append(right.toList().dropLast(1).toBranch(measure)))
            }
        }
    }

    // detach the leftmost item from the tree (if any)
    private fun <V: Any, T : Any> Tree<V, T>.viewLeft(): Pair<T?, Tree<V, T>> {
        return when (this) {
            is Tree.Empty<V, T> -> Pair(null, this)
            is Tree.Single<V, T> -> Pair(value, Tree.Empty(measure))
            is Tree.Deep<V, T> -> when (left) {
                is Digit.Four<T> -> Pair(left.first, replace(left = listOf(left.second, left.third, left.fourth)))
                is Digit.Three<T> -> Pair(left.first, replace(left = listOf(left.second, left.third)))
                is Digit.Two<T> -> Pair(left.first, replace(left = listOf(left.second)))
                is Digit.One<T> -> {

                    // run out of nodes at this level, extract from branch at lower level, careful when lower
                    // level is empty
                    val (branch, reducedMiddle) = middle.viewLeft()
                    when (branch) {
                        null -> Pair(left.first, createSimpleTree(right.toList(), measure))
                        else -> Pair(left.first, replace(left = branch.toList(), middle = reducedMiddle))
                    }
                }
            }
        }
    }

    // detach the rightmost item from the tree (if any)
    private fun <V: Any, T : Any> Tree<V, T>.viewRight(): Pair<T?, Tree<V, T>> {
        return when (this) {
            is Tree.Empty<V, T> -> Pair(null, this)
            is Tree.Single<V, T> -> Pair(value, Tree.Empty(measure))
            is Tree.Deep<V, T> -> when (right) {
                is Digit.Four<T> -> Pair(right.fourth, replace(right = listOf(right.first, right.second, right.third)))
                is Digit.Three<T> -> Pair(right.third, replace(right = listOf(right.first, right.second)))
                is Digit.Two<T> -> Pair(right.second, replace(right = listOf(right.first)))
                is Digit.One<T> -> {

                    // run out of nodes at this level, extract from branch at lower level, careful when no nodes
                    val (branch, reducedMiddle) = middle.viewRight()
                    when (branch) {
                        null -> Pair(right.first, createSimpleTree(left.toList(), measure))
                        else -> Pair(right.first, replace(right = branch.toList(), middle = reducedMiddle))
                    }
                }
            }
        }
    }

    // concatenates two trees with random stuff in the middle (makes sure measures are the same first)
    // deals with easy cases (Single & Empty), and then globs up the tail / items to be pushed down into the
    // rest of the tree
    fun <V : Any, T : Any> concatWithMiddle(left: Tree<V, T>, items: List<T>, right: Tree<V, T>): Tree<V, T> {
        assert(left.measure == right.measure) { "Measures need to be the same to concat" }
        val measure = left.measure
        return when {
            left is Tree.Empty<V, T> -> when {
                items.isEmpty() -> right
                else -> concatWithMiddle(Tree.Empty(measure), items.drop(1), right).prepend(items.first())
            }
            left is Tree.Single<V, T> -> {
                concatWithMiddle(Tree.Empty(measure), items, right).prepend(left.value)
            }
            right is Tree.Empty<V, T> -> when {
                items.isEmpty() -> left
                else -> concatWithMiddle(left, items.dropLast(1), Tree.Empty(measure)).append(items.last())
            }
            right is Tree.Single<V, T> -> {
                concatWithMiddle(left, items, Tree.Empty(measure)).append(right.value)
            }
            left is Tree.Deep<V, T> && right is Tree.Deep<V, T> -> left.replace(
                left = left.left.toList(),
                middle = concatWithMiddle(
                    left = left.middle,
                    items = (left.right.toList() + items + right.left.toList()).toBranches(measure),
                    right = right.middle,
                ),
                right = right.right.toList(),
            )
            else -> error("above is actually exhaustive, but need exception for IntelliSense, shouldn't get here")
        }
    }

    // handy overload for concatenating two trees without explicitly mentioning items in the middle
    fun <V : Any, T : Any> concat(left: Tree<V, T>, right: Tree<V, T>) =
        concatWithMiddle(left, emptyList(), right)

    /**
     * Tree Splitting with annotations
     */

    private data class Split<V : Any, T : Any>(val before: Tree<V, T>, val pivot: V, val after: Tree<V, T>)

//    private fun <V : Any, T : Any> Tree<V, T>.split(predicate: (V) -> Boolean, pivot: V): Split<V, T> {
//        when (this) {
//
//            // simple cases
//            is Tree.Empty<V, T> -> {
//                error("Split point not found.")
//            }
//            is Tree.Single<V, T> -> when  {
//                predicate(measure.combine(pivot, measure(value))) -> {
//                    return Split(Tree.Empty(measure), pivot, Tree.Empty(measure))
//                }
//                else -> {
//                    error("Split point not found.")
//                }
//            }
//
//            // interesting case
//            is Tree.Deep<V, T> -> run {
//
//                // sanity check, make sure split point is in the tree
//                if (!predicate(measure.combine(pivot, annotation))) {
//                    error("Split point not found.")
//                }
//
//                // split is in the prefix
//                return left.toList().let { leftList ->
//                    leftList.indices
//                        .filter { index ->
//                            predicate(measure.combine(pivot, measure(leftList.slice(0 .. index))))
//                        }
//                        .map { index ->
//                            Split(
//                                left = createSimpleTree(leftList.slice(0 .. index), measure),
//                                pivot =
//                            )
//                        }
//                        .first()
//                }
//
//                if (predicate(measure(left))) {
//                    val (before, after) = left.toList().partition { predicate(measure(it)) }
//                    return Split(createSimpleTree(before, measure), )
//                }
//
//
//            }
//        }
//    }

    fun <V : Any, T : Any> Tree<V, T>.debugText(indent: String = ""): String {
        val tree = this

        fun Any.debugText(): String = when (this) {
            is Digit<*> -> this
                .toList()
                .joinToString(prefix = "(", separator = ", ", postfix = ")") { it.debugText() }
            is Branch<*, *> -> this
                .toList()
                .joinToString(prefix = "(", separator = ", ", postfix = ")") { it.debugText() }
            else -> this.toString()
        }

        return buildString {
            when (tree) {
                is Tree.Empty<V, T> -> appendLine("${indent}Empty")
                is Tree.Single<V, T> -> appendLine("${indent}Single: ${tree.value.debugText()}")
                is Tree.Deep<V, T> -> {
                    appendLine("${indent}Left: ${tree.left.debugText()}")
                    appendLine("${indent}Middle:")
                    append(tree.middle.debugText("$indent  "))
                    appendLine("${indent}Right: ${tree.right.debugText()}")
                }
            }
        }
    }

    fun runTest() {
        val measure = object : Measure<Int, Int> {
            override val empty: Int = 0
            override fun combine(a: Int, b: Int) = a + b
            override fun invoke(item: Int): Int = 1
        }
        val emptyTree: Tree<Int, Int> = Tree.Empty(measure)
        val tree = (1 .. 500).fold(emptyTree) { tree, x -> tree.append(x) }
        val tailTree = (1 .. 34).fold(tree) { t, _ -> t.viewLeft().second }
        println(tailTree.debugText())

    }

    companion object {
        operator fun <V: Any, T: Any> invoke(measure: Measure<V, T>) = FingerTree(Tree.Empty(measure))

        fun runTest() {
            val measure = object : Measure<Int, Int> {
                override val empty: Int = 0
                override fun combine(a: Int, b: Int) = a + b
                override fun invoke(item: Int): Int = 1
            }
            FingerTree(measure).runTest()
        }
    }
}
