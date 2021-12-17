//class FingerTree<T: Any> private constructor(root: Tree<T, T>, private val comparator: Comparator<T>) {
//    private val root: Tree<T, T> = Tree.Empty()
//
//    companion object {
//        operator fun <T: Comparable<T>> invoke() = FingerTree(Tree.Empty(), compareBy<T> { it })
//        operator fun <T : Any> invoke(comparator: Comparator<T>) = FingerTree(Tree.Empty(), comparator)
//    }
//
//    sealed class Tree<V : Any, T : Any> {
//        class Empty<V : Any, T : Any> : Tree<V, T>()
//        data class Single<V : Any, T : Any>(val size: Int, val value: T) : Tree<V, T>()
//        data class Deep<V : Any, T : Any>(
//            val max: V,
//            val size: Int,
//            val left: Digit<T>,
//            val middle: Tree<V, Branch<V, T>>,
//            val right: Digit<T>
//        ) : Tree<V, T>()
//    }
//
//    sealed class Branch<V : Any, T : Any> {
//        abstract val size: Int
//        abstract val max: V
//
//        data class Branch2<V : Any, T : Any>(
//            override val size: Int,
//            override val max: V,
//            val first: T,
//            val second: T,
//        ) : Branch<V, T>()
//        data class Branch3<V : Any, T : Any>(
//            override val size: Int,
//            override val max: V,
//            val first: T,
//            val second: T,
//            val third: T,
//        ) : Branch<V, T>()
//    }
//
//    sealed class Digit<T : Any> {
//        data class One<T : Any>(val first: T) : Digit<T>()
//        data class Two<T : Any>(val first: T, val second: T) : Digit<T>()
//        data class Three<T : Any>(val first: T, val second: T, val third: T) : Digit<T>()
//        data class Four<T : Any>(val first: T, val second: T, val third: T, val fourth: T) : Digit<T>()
//    }
//
//    // helper functions
//    private fun <T : Any> getSize(vararg items: T): Int = items.sumOf { item ->
//        when (item) {
//            is Branch<*, *> -> item.size
//            is Digit.One<*> -> getSize(item.first)
//            is Digit.Two<*> -> getSize(item.first, item.second)
//            is Digit.Three<*> -> getSize(item.first, item.second, item.third)
//            is Digit.Four<*> -> getSize(item.first, item.second, item.third, item.fourth)
//            else -> 1
//        }
//    }
//
//    private fun <V : Any, T : Any> getMax(vararg items: T): V {
//        val maxes = items.map { item ->
//            when (item) {
//                is Branch<*, *> -> item.max
//                is Digit.One<*> -> getMax(item.first)
//                is Digit.Two<*> -> getMax(item.first, item.second)
//                is Digit.Three<*> -> getMax(item.first, item.second, item.third)
//                is Digit.Four<*> -> getMax(item.first, item.second, item.third, item.fourth)
//                else -> item as T
//            }
//        }
//        return maxes.max
//    }
//
//    fun <V : Any, T : Any> Tree<V, T>.prepend(item: T): Tree<V, T> = when (this) {
//        is Tree.Empty<V, T> -> Tree.Single(getSize(item), item)
//        is Tree.Single<V, T> -> Tree.Deep(Digit.One(item), Tree.Empty(), Digit.One(value))
//        is Tree.Deep<V, T> -> {
//            when (left) {
//                is Digit.One<T> -> copy(left = Digit.Two(item, left.first))
//                is Digit.Two<T> -> copy(left = Digit.Three(item, left.first, left.second))
//                is Digit.Three<T> -> copy(left = Digit.Four(item, left.first, left.second, left.third))
//                is Digit.Four<T> -> copy(
//                    left = Digit.Two(item, left.first),
//                    middle = middle.prepend(Branch.Branch3(left.second, left.third, left.fourth)),
//                )
//            }
//        }
//    }
//
//    fun <T : Any> Tree<T>.append(item: T): Tree<T> = when (this) {
//        is Tree.Empty<T> -> Tree.Single(item)
//        is Tree.Single<T> -> Tree.Deep(Digit.One(value), Tree.Empty(), Digit.One(item))
//        is Tree.Deep<T> -> {
//            when (right) {
//                is Digit.One<T> -> copy(right = Digit.Two(right.first, item))
//                is Digit.Two<T> -> copy(right = Digit.Three(right.first, right.second, item))
//                is Digit.Three<T> -> copy(right = Digit.Four(right.first, right.second, right.third, item))
//                is Digit.Four<T> -> copy(
//                    middle = middle.append(Branch.Branch3(right.first, right.second, right.third)),
//                    right = Digit.Two(right.fourth, item)
//                )
//            }
//        }
//    }
//
//    private fun <T : Any> Tree<T>.viewLeft(): Pair<T?, Tree<T>> {
//        return when (this) {
//            is Tree.Empty<T> -> Pair(null, this)
//            is Tree.Single<T> -> Pair(value, Tree.Empty())
//            is Tree.Deep<T> -> when (left) {
//                is Digit.Four<T> -> Pair(left.first, copy(left = Digit.Three(left.second, left.third, left.fourth)))
//                is Digit.Three<T> -> Pair(left.first, copy(left = Digit.Two(left.second, left.third)))
//                is Digit.Two<T> -> Pair(left.first, copy(left = Digit.One(left.second)))
//                is Digit.One<T> -> {
//                    val (branch, restMiddle) = middle.viewLeft()
//                    when (branch) {
//                        null -> Pair(
//                            left.first,
//                            when (right) {
//                                is Digit.Four<T> -> Tree.Deep(
//                                    left = Digit.Two(right.first, right.second),
//                                    middle = Tree.Empty(),
//                                    right = Digit.Two(right.third, right.fourth),
//                                )
//                                is Digit.Three<T> -> Tree.Deep(
//                                    left = Digit.Two(right.first, right.second),
//                                    middle = Tree.Empty(),
//                                    right = Digit.One(right.third),
//                                )
//                                is Digit.Two<T> -> Tree.Deep(
//                                    left = Digit.One(right.first),
//                                    middle = Tree.Empty(),
//                                    right = Digit.One(right.second),
//                                )
//                                is Digit.One<T> -> Tree.Single(right.first)
//                            }
//                        )
//                        is Branch.Branch2<T> -> Pair(
//                            left.first,
//                            copy(left = Digit.Two(branch.first, branch.second), middle = restMiddle),
//                        )
//                        is Branch.Branch3<T> -> Pair(
//                            left.first,
//                            copy(left = Digit.Three(branch.first, branch.second, branch.third), middle = restMiddle),
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//    private fun <T : Any> Tree<T>.viewRight(): Pair<T?, Tree<T>> {
//        return when (this) {
//            is Tree.Empty<T> -> Pair(null, this)
//            is Tree.Single<T> -> Pair(value, Tree.Empty())
//            is Tree.Deep<T> -> when (right) {
//                is Digit.Four<T> -> Pair(
//                    right.fourth,
//                    copy(right = Digit.Three(right.first, right.second, right.third))
//                )
//                is Digit.Three<T> -> Pair(right.third, copy(right = Digit.Two(right.first, right.second)))
//                is Digit.Two<T> -> Pair(right.second, copy(right = Digit.One(right.first)))
//                is Digit.One<T> -> {
//                    val (branch, restMiddle) = middle.viewRight()
//                    when (branch) {
//                        null -> Pair(
//                            right.first,
//                            when (left) {
//                                is Digit.Four<T> -> Tree.Deep(
//                                    left = Digit.Two(left.first, left.second),
//                                    middle = Tree.Empty(),
//                                    right = Digit.Two(left.third, left.fourth),
//                                )
//                                is Digit.Three<T> -> Tree.Deep(
//                                    left = Digit.One(left.first),
//                                    middle = Tree.Empty(),
//                                    right = Digit.Two(left.second, left.third),
//                                )
//                                is Digit.Two<T> -> Tree.Deep(
//                                    left = Digit.One(left.first),
//                                    middle = Tree.Empty(),
//                                    right = Digit.One(left.second),
//                                )
//                                is Digit.One<T> -> Tree.Single(left.first)
//                            }
//                        )
//                        is Branch.Branch2<T> -> Pair(
//                            right.first,
//                            copy(middle = restMiddle, right = Digit.Two(branch.first, branch.second)),
//                        )
//                        is Branch.Branch3<T> -> Pair(
//                            right.first,
//                            copy(middle = restMiddle, right = Digit.Three(branch.first, branch.second, branch.third)),
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//    // helper function to "chunk up" odds and ends into deeper branches
//    fun <T : Any> Iterable<T>.toBranches(): List<Branch<T>> {
//        val chunks = this.chunked(2)
//        return when {
//            chunks.size <= 1 && chunks.flatten().isEmpty() -> error("Not enough items to convert to branches (0).")
//            chunks.size <= 1 && chunks.flatten().size == 1 -> error("Not enough items to convert to branches (1).")
//            chunks.last().size == 1 -> {
//                chunks.dropLast(2).map { Branch.Branch2(it[0], it[1]) } +
//                        listOf(chunks.takeLast(2).flatten().let { Branch.Branch3(it[0], it[1], it[2]) })
//            }
//            else -> chunks.map { Branch.Branch2(it[0], it[1]) }
//        }
//    }
//
//    fun <T : Any> concatWithMiddle(left: Tree<T>, items: List<T>, right: Tree<T>): Tree<T> {
//        return when {
//            left is Tree.Empty<T> -> when {
//                items.isEmpty() -> right
//                else -> concatWithMiddle(Tree.Empty(), items.drop(1), right).prepend(items.first())
//            }
//            left is Tree.Single<T> -> {
//                concatWithMiddle(Tree.Empty(), items, right).prepend(left.value)
//            }
//            right is Tree.Empty<T> -> when {
//                items.isEmpty() -> left
//                else -> concatWithMiddle(left, items.dropLast(1), Tree.Empty()).append(items.last())
//            }
//            right is Tree.Single<T> -> {
//                concatWithMiddle(left, items, Tree.Empty()).append(right.value)
//            }
//            left is Tree.Deep<T> && right is Tree.Deep<T> -> Tree.Deep(
//                left = left.left,
//                middle = concatWithMiddle(left.middle, (left.right + items + right.left).toBranches(), right.middle),
//                right = right.right,
//            )
//            else -> error("above is actually exhaustive, but need exception for IntelliSense, shouldn't get here")
//        }
//    }
//
//    fun <T : Any> concat(left: Tree<T>, right: Tree<T>) = concatWithMiddle(left, emptyList(), right)
//}
