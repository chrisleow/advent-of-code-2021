//sealed class OrderedFingerTree<T: Any, V: Comparable<V>> : Iterable<T> {
//    abstract val measure: (T) -> V
//
//    data class Empty<T: Any, V: Comparable<V>>(
//        override val measure: (T) -> V) : OrderedFingerTree<T, V>()
//    data class Single<T: Any, V: Comparable<V>>(
//        override val measure: (T) -> V,
//        val value: T) : OrderedFingerTree<T, V>()
//    data class Deep<T: Any, V: Comparable<V>>(
//        override val measure: (T) -> V,
//        val annotation: V,
//        val left: Digit<T>,
//        val middle: OrderedFingerTree<OrderedBranch<T, V>, V>,
//        val right: Digit<T>) : OrderedFingerTree<T, V>()
//
//    override fun iterator(): Iterator<T> {
//        val sequence: Sequence<T> = when (this) {
//            is Empty<T, *> -> emptySequence()
//            is Single<T, *> -> sequenceOf(this.value)
//            is Deep<T, *> -> sequence {
//                yieldAll(left.iterator().asSequence())
//                yieldAll(middle.iterator().asSequence().flatMap {
//                    when (it) {
//                        is OrderedBranch.Branch2<T, *> -> sequenceOf(it.first, it.second)
//                        is OrderedBranch.Branch3<T, *> -> sequenceOf(it.first, it.second, it.third)
//                    }
//                })
//                yieldAll(right.iterator().asSequence())
//            }
//        }
//        return sequence.iterator()
//    }
//
//    val size: Int by lazy {
//        when (this) {
//            is Empty<*, *> -> 0
//            is Single<*, *> -> if (value is OrderedBranch<*, *>) value.size else 1
//            is Deep<*, *> -> left.size + middle.size + right.size
//        }
//    }
//}
//
//sealed class OrderedBranch<T: Any, V: Comparable<V>> : Iterable<T> {
//    abstract val annotation: V
//    data class Branch2<T: Any, V: Comparable<V>>(
//        override val annotation: V,
//        val first: T,
//        val second: T) : OrderedBranch<T, V>()
//    data class Branch3<T: Any, V: Comparable<V>>(
//        override val annotation: V,
//        val first: T,
//        val second: T,
//        val third: T) : OrderedBranch<T, V>()
//
//    private val valuesSequence: Sequence<T> by lazy {
//        when (this) {
//            is Branch2<T, *> -> sequenceOf(first, second)
//            is Branch3<T, *> -> sequenceOf(first, second, third)
//        }
//    }
//
//    override fun iterator(): Iterator<T> = valuesSequence.iterator()
//    val size: Int = this.iterator().asSequence().sumOf { if (it is OrderedBranch<*, *>) it.size else 1 }
//}
//
//data class Split<T: Any, V: Comparable<V>>(
//    val left: OrderedFingerTree<T, V>,
//    val pivot: T?,
//    val right: OrderedFingerTree<T, V>)
//
////fun <T: Comparable<T>> OrderedFingerTree<T>.split(pivot: T): Split<T> {
////    return when (this) {
////        is OrderedFingerTree.Empty<T> -> Split(OrderedFingerTree.Empty(), null, OrderedFingerTree.Empty())
////        is OrderedFingerTree.Single<T> -> when {
////            pivot < value -> Split(OrderedFingerTree.Empty(), null, OrderedFingerTree.Single(value))
////            pivot > value -> Split(OrderedFingerTree.Single(value), null, OrderedFingerTree.Empty())
////            else -> Split(OrderedFingerTree.Empty(), pivot, OrderedFingerTree.Empty())
////        }
////        is OrderedFingerTree.Deep<T> -> when {
////
////        }
////    }
////}
//
////fun <T: Comparable<T>> OrderedFingerTree<T>.add(item: T): OrderedFingerTree<T> {
////    return when (this) {
////        is OrderedFingerTree.Empty<T> -> OrderedFingerTree.Single(item)
////        is OrderedFingerTree.Single<T> -> when (item < value) {
////            true -> OrderedFingerTree.Deep(item, Digit.One(item), OrderedFingerTree.Empty(), Digit.One(value))
////            false -> OrderedFingerTree.Deep(item, Digit.One(value), OrderedFingerTree.Empty(), Digit.One(item))
////        }
////        is OrderedFingerTree.Deep<T> -> {
////            when {
////
////            }
////        }
////    }
////}
//
//fun <T: Any, V: Comparable<V>> OrderedFingerTree<T, V>.prepend(item: T): OrderedFingerTree<T, V> = when (this) {
//    is OrderedFingerTree.Empty<T, V> -> OrderedFingerTree.Single(measure, item)
//    is OrderedFingerTree.Single<T, V> -> OrderedFingerTree.Deep(
//        measure = measure,
//        annotation = measure(value),
//        left = Digit.One(item),
//        middle = OrderedFingerTree.Empty { it.annotation },
//        right = Digit.One(value),
//    )
//    is OrderedFingerTree.Deep<T, V> -> {
//        when (left) {
//            is Digit.One<T> -> copy(left = Digit.Two(item, left.first))
//            is Digit.Two<T> -> copy(left = Digit.Three(item, left.first, left.second))
//            is Digit.Three<T> -> copy(left = Digit.Four(item, left.first, left.second, left.third))
//            is Digit.Four<T> -> copy(
//                left = Digit.Two(item, left.first),
//                middle = middle.prepend(
//                    OrderedBranch.Branch3(measure(left.fourth), left.second, left.third, left.fourth)),
//            )
//        }
//    }
//}
//
//fun <T: Any, V: Comparable<V>> OrderedFingerTree<T, V>.append(item: T): OrderedFingerTree<T, V> = when (this) {
//    is OrderedFingerTree.Empty<T, V> -> OrderedFingerTree.Single(measure, item)
//    is OrderedFingerTree.Single<T, V> -> OrderedFingerTree.Deep(
//        measure = measure,
//        annotation = measure(item),
//        left = Digit.One(value),
//        middle = OrderedFingerTree.Empty { it.annotation },
//        right = Digit.One(item))
//    is OrderedFingerTree.Deep<T, V> -> {
//        when (right) {
//            is Digit.One<T> -> copy(
//                annotation = measure(item),
//                right = Digit.Two(right.first, item))
//            is Digit.Two<T> -> copy(
//                annotation = measure(item),
//                right = Digit.Three(right.first, right.second, item))
//            is Digit.Three<T> -> copy(
//                annotation = measure(item),
//                right = Digit.Four(right.first, right.second, right.third, item))
//            is Digit.Four<T> -> copy(
//                annotation = measure(item),
//                middle = middle.append(
//                    OrderedBranch.Branch3(measure(right.third), right.first, right.second, right.third)),
//                right = Digit.Two(right.fourth, item)
//            )
//        }
//    }
//}
//
//private fun <T: Any, V: Comparable<V>> OrderedFingerTree<T, V>.viewLeft(): Pair<T?, OrderedFingerTree<T, V>> {
//    return when (this) {
//        is OrderedFingerTree.Empty<T, V> -> Pair(null, this)
//        is OrderedFingerTree.Single<T, V> -> Pair(value, OrderedFingerTree.Empty(measure))
//        is OrderedFingerTree.Deep<T, V> -> when (left) {
//            is Digit.Four<T> -> Pair(left.first, copy(left = Digit.Three(left.second, left.third, left.fourth)))
//            is Digit.Three<T> -> Pair(left.first, copy(left = Digit.Two(left.second, left.third)))
//            is Digit.Two<T> -> Pair(left.first, copy(left = Digit.One(left.second)))
//            is Digit.One<T> -> {
//                val (branch, restMiddle) = middle.viewLeft()
//                when (branch) {
//                    null -> Pair(
//                        left.first,
//                        when (right) {
//                            is Digit.Four<T> -> OrderedFingerTree.Deep(
//                                measure = measure,
//                                annotation = measure(right.fourth),
//                                left = Digit.Two(right.first, right.second),
//                                middle = OrderedFingerTree.Empty { it.annotation },
//                                right = Digit.Two(right.third, right.fourth),
//                            )
//                            is Digit.Three<T> -> OrderedFingerTree.Deep(
//                                measure = measure,
//                                annotation = measure(right.third),
//                                left = Digit.Two(right.first, right.second),
//                                middle = OrderedFingerTree.Empty { it.annotation },
//                                right = Digit.One(right.third),
//                            )
//                            is Digit.Two<T> -> OrderedFingerTree.Deep(
//                                measure = measure,
//                                annotation = measure(right.second),
//                                left = Digit.One(right.first),
//                                middle = OrderedFingerTree.Empty { it.annotation },
//                                right = Digit.One(right.second),
//                            )
//                            is Digit.One<T> -> OrderedFingerTree.Single(measure, right.first)
//                        }
//                    )
//                    is OrderedBranch.Branch2<T, V> -> Pair(
//                        left.first,
//                        copy(left = Digit.Two(branch.first, branch.second), middle = restMiddle),
//                    )
//                    is OrderedBranch.Branch3<T, V> -> Pair(
//                        left.first,
//                        copy(left = Digit.Three(branch.first, branch.second, branch.third), middle = restMiddle),
//                    )
//                }
//            }
//        }
//    }
//}
//
//private fun <T: Any, V: Comparable<V>> OrderedFingerTree<T, V>.viewRight(): Pair<T?, OrderedFingerTree<T, V>> {
//    return when (this) {
//        is OrderedFingerTree.Empty<T, V> -> Pair(null, this)
//        is OrderedFingerTree.Single<T, V> -> Pair(value, OrderedFingerTree.Empty(measure))
//        is OrderedFingerTree.Deep<T, V> -> when (right) {
//            is Digit.Four<T> -> Pair(
//                right.fourth,
//                copy(annotation = measure(right.third), right = Digit.Three(right.first, right.second, right.third)))
//            is Digit.Three<T> -> Pair(
//                right.third,
//                copy(annotation = measure(right.second), right = Digit.Two(right.first, right.second)))
//            is Digit.Two<T> -> Pair(
//                right.second,
//                copy(annotation = measure(right.first), right = Digit.One(right.first)))
//            is Digit.One<T> -> {
//                val (branch, restMiddle) = middle.viewRight()
//                when (branch) {
//                    null -> Pair(
//                        right.first,
//                        when (left) {
//                            is Digit.Four<T> -> OrderedFingerTree.Deep(
//                                measure = measure,
//                                annotation = measure(left.fourth),
//                                left = Digit.Two(left.first, left.second),
//                                middle = OrderedFingerTree.Empty { it.annotation },
//                                right = Digit.Two(left.third, left.fourth),
//                            )
//                            is Digit.Three<T> -> OrderedFingerTree.Deep(
//                                measure = measure,
//                                annotation = measure(left.third),
//                                left = Digit.One(left.first),
//                                middle = OrderedFingerTree.Empty { it.annotation },
//                                right = Digit.Two(left.second, left.third),
//                            )
//                            is Digit.Two<T> -> OrderedFingerTree.Deep(
//                                measure = measure,
//                                annotation = measure(left.second),
//                                left = Digit.One(left.first),
//                                middle = OrderedFingerTree.Empty { it.annotation },
//                                right = Digit.One(left.second),
//                            )
//                            is Digit.One<T> -> OrderedFingerTree.Single(measure, left.first)
//                        }
//                    )
//                    is OrderedBranch.Branch2<T, V> -> Pair(
//                        right.first,
//                        copy(
//                            annotation = measure(branch.second),
//                            middle = restMiddle,
//                            right = Digit.Two(branch.first, branch.second)),
//                    )
//                    is OrderedBranch.Branch3<T, V> -> Pair(
//                        right.first,
//                        copy(
//                            annotation = measure(branch.third),
//                            middle = restMiddle,
//                            right = Digit.Three(branch.first, branch.second, branch.third)),
//                    )
//                }
//            }
//        }
//    }
//}
//
//// helper function to "chunk up" odds and ends into deeper branches
//fun <T: Any, V: Comparable<V>> Iterable<T>.toAnnotatedBranches(measure: (T) -> V): List<OrderedBranch<T, V>> {
//    val chunks = this.chunked(2)
//    return when {
//        chunks.size <= 1 && chunks.flatten().isEmpty() -> error("Not enough items to convert to branches (0).")
//        chunks.size <= 1 && chunks.flatten().size == 1 -> error("Not enough items to convert to branches (1).")
//        chunks.last().size == 1 -> {
//            val head = chunks.dropLast(2).map {
//                OrderedBranch.Branch2(maxOf(measure(it[0]), measure(it[1])), it[0], it[1])
//            }
//            val last = chunks.takeLast(2).flatten().let {
//                OrderedBranch.Branch3(maxOf(measure(it[0]), measure(it[1]), measure(it[2])), it[0], it[1], it[2])
//            }
//            head + listOf(last)
//        }
//        else -> chunks.map { OrderedBranch.Branch2(maxOf(measure(it[0]), measure(it[1])), it[0], it[1])}
//    }
//}
//
//fun <T: Any, V: Comparable<V>> concatWithMiddle(
//    left: OrderedFingerTree<T, V>,
//    items: List<T>,
//    right: OrderedFingerTree<T, V>): OrderedFingerTree<T, V> {
//    return when {
//        left is OrderedFingerTree.Empty<T, V> -> when {
//            items.isEmpty() -> right
//            else -> concatWithMiddle(OrderedFingerTree.Empty(left.measure), items.drop(1), right).prepend(items.first())
//        }
//        left is OrderedFingerTree.Single<T> -> {
//            concatWithMiddle(FingerTree.Empty(), items, right).prepend(left.value)
//        }
//        right is OrderedFingerTree.Empty<T> -> when {
//            items.isEmpty() -> left
//            else -> concatWithMiddle(left, items.dropLast(1), FingerTree.Empty()).append(items.last())
//        }
//        right is OrderedFingerTree.Single<T> -> {
//            concatWithMiddle(left, items, FingerTree.Empty()).append(right.value)
//        }
//        left is OrderedFingerTree.Deep<T> && right is OrderedFingerTree.Deep<T> -> OrderedFingerTree.Deep(
//            left = left.left,
//            middle = concatWithMiddle(left.middle, (left.right + items + right.left).toBranches(), right.middle),
//            right = right.right,
//        )
//        else -> error("above is actually exhaustive, but need exception for IntelliSense, shouldn't get here")
//    }
//}
//
//fun main() {
//    val emptyTree: OrderedFingerTree<Int, Int> = OrderedFingerTree.Empty { it }
//    val tree = (1 .. 100).fold(emptyTree) { tree, x -> tree.append(x) }
//    val (item, tailTree) = tree.viewRight()
//    println(item)
//    println(tailTree.toList())
//}
