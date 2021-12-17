//sealed class FingerTree<T : Any> : Iterable<T> {
//    class Empty<T : Any> : FingerTree<T>()
//    data class Single<T : Any>(val value: T) : FingerTree<T>()
//    data class Deep<T : Any>(
//        val left: Digit<T>,
//        val middle: FingerTree<Branch<T>>,
//        val right: Digit<T>
//    ) : FingerTree<T>()
//
//    override fun iterator(): Iterator<T> {
//        val sequence: Sequence<T> = when (this) {
//            is Empty<T> -> emptySequence()
//            is Single<T> -> sequenceOf(this.value)
//            is Deep<T> -> sequence {
//                yieldAll(left.iterator().asSequence())
//                yieldAll(middle.iterator().asSequence().flatMap {
//                    when (it) {
//                        is Branch.Branch2<T> -> sequenceOf(it.first, it.second)
//                        is Branch.Branch3<T> -> sequenceOf(it.first, it.second, it.third)
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
//            is Empty<*> -> 0
//            is Single<*> -> if (value is Branch<*>) value.size else 1
//            is Deep<*> -> left.size + middle.size + right.size
//        }
//    }
//}
//
//sealed class Branch<T : Any> : Iterable<T>, Comparable<Branch<T>> {
//    data class Branch2<T : Any>(val first: T, val second: T) : Branch<T>()
//    data class Branch3<T : Any>(val first: T, val second: T, val third: T) : Branch<T>()
//
//    override fun compareTo(other: Branch<T>): Int {
//        TODO("Not yet implemented")
//    }
//
//    override fun iterator(): Iterator<T> {
//        val sequence = when (this) {
//            is Branch2<T> -> sequenceOf(first, second)
//            is Branch3<T> -> sequenceOf(first, second, third)
//        }
//        return sequence.iterator()
//    }
//
//    val size: Int = this.iterator().asSequence().sumOf { if (it is Branch<*>) it.size else 1 }
//}
//
//sealed class Digit<T : Any> : Iterable<T> {
//    data class One<T : Any>(val first: T) : Digit<T>()
//    data class Two<T : Any>(val first: T, val second: T) : Digit<T>()
//    data class Three<T : Any>(val first: T, val second: T, val third: T) : Digit<T>()
//    data class Four<T : Any>(val first: T, val second: T, val third: T, val fourth: T) : Digit<T>()
//
//    override fun iterator(): Iterator<T> {
//        val sequence = when (this) {
//            is One<T> -> sequenceOf(first)
//            is Two<T> -> sequenceOf(first, second)
//            is Three<T> -> sequenceOf(first, second, third)
//            is Four<T> -> sequenceOf(first, second, third, fourth)
//        }
//        return sequence.iterator()
//    }
//
//    val size: Int = this.iterator().asSequence().sumOf { if (it is Branch<*>) it.size else 1 }
//}
//
//fun <T : Any> FingerTree<T>.prepend(item: T): FingerTree<T> = when (this) {
//    is FingerTree.Empty<T> -> FingerTree.Single(item)
//    is FingerTree.Single<T> -> FingerTree.Deep(Digit.One(item), FingerTree.Empty(), Digit.One(value))
//    is FingerTree.Deep<T> -> {
//        when (left) {
//            is Digit.One<T> -> copy(left = Digit.Two(item, left.first))
//            is Digit.Two<T> -> copy(left = Digit.Three(item, left.first, left.second))
//            is Digit.Three<T> -> copy(left = Digit.Four(item, left.first, left.second, left.third))
//            is Digit.Four<T> -> copy(
//                left = Digit.Two(item, left.first),
//                middle = middle.prepend(Branch.Branch3(left.second, left.third, left.fourth)),
//            )
//        }
//    }
//}
//
//fun <T : Any> FingerTree<T>.append(item: T): FingerTree<T> = when (this) {
//    is FingerTree.Empty<T> -> FingerTree.Single(item)
//    is FingerTree.Single<T> -> FingerTree.Deep(Digit.One(value), FingerTree.Empty(), Digit.One(item))
//    is FingerTree.Deep<T> -> {
//        when (right) {
//            is Digit.One<T> -> copy(right = Digit.Two(right.first, item))
//            is Digit.Two<T> -> copy(right = Digit.Three(right.first, right.second, item))
//            is Digit.Three<T> -> copy(right = Digit.Four(right.first, right.second, right.third, item))
//            is Digit.Four<T> -> copy(
//                middle = middle.append(Branch.Branch3(right.first, right.second, right.third)),
//                right = Digit.Two(right.fourth, item)
//            )
//        }
//    }
//}
//
//private fun <T : Any> FingerTree<T>.viewLeft(): Pair<T?, FingerTree<T>> {
//    return when (this) {
//        is FingerTree.Empty<T> -> Pair(null, this)
//        is FingerTree.Single<T> -> Pair(value, FingerTree.Empty())
//        is FingerTree.Deep<T> -> when (left) {
//            is Digit.Four<T> -> Pair(left.first, copy(left = Digit.Three(left.second, left.third, left.fourth)))
//            is Digit.Three<T> -> Pair(left.first, copy(left = Digit.Two(left.second, left.third)))
//            is Digit.Two<T> -> Pair(left.first, copy(left = Digit.One(left.second)))
//            is Digit.One<T> -> {
//                val (branch, restMiddle) = middle.viewLeft()
//                when (branch) {
//                    null -> Pair(
//                        left.first,
//                        when (right) {
//                            is Digit.Four<T> -> FingerTree.Deep(
//                                left = Digit.Two(right.first, right.second),
//                                middle = FingerTree.Empty(),
//                                right = Digit.Two(right.third, right.fourth),
//                            )
//                            is Digit.Three<T> -> FingerTree.Deep(
//                                left = Digit.Two(right.first, right.second),
//                                middle = FingerTree.Empty(),
//                                right = Digit.One(right.third),
//                            )
//                            is Digit.Two<T> -> FingerTree.Deep(
//                                left = Digit.One(right.first),
//                                middle = FingerTree.Empty(),
//                                right = Digit.One(right.second),
//                            )
//                            is Digit.One<T> -> FingerTree.Single(right.first)
//                        }
//                    )
//                    is Branch.Branch2<T> -> Pair(
//                        left.first,
//                        copy(left = Digit.Two(branch.first, branch.second), middle = restMiddle),
//                    )
//                    is Branch.Branch3<T> -> Pair(
//                        left.first,
//                        copy(left = Digit.Three(branch.first, branch.second, branch.third), middle = restMiddle),
//                    )
//                }
//            }
//        }
//    }
//}
//
//private fun <T : Any> FingerTree<T>.viewRight(): Pair<T?, FingerTree<T>> {
//    return when (this) {
//        is FingerTree.Empty<T> -> Pair(null, this)
//        is FingerTree.Single<T> -> Pair(value, FingerTree.Empty())
//        is FingerTree.Deep<T> -> when (right) {
//            is Digit.Four<T> -> Pair(
//                right.fourth,
//                copy(right = Digit.Three(right.first, right.second, right.third))
//            )
//            is Digit.Three<T> -> Pair(right.third, copy(right = Digit.Two(right.first, right.second)))
//            is Digit.Two<T> -> Pair(right.second, copy(right = Digit.One(right.first)))
//            is Digit.One<T> -> {
//                val (branch, restMiddle) = middle.viewRight()
//                when (branch) {
//                    null -> Pair(
//                        right.first,
//                        when (left) {
//                            is Digit.Four<T> -> FingerTree.Deep(
//                                left = Digit.Two(left.first, left.second),
//                                middle = FingerTree.Empty(),
//                                right = Digit.Two(left.third, left.fourth),
//                            )
//                            is Digit.Three<T> -> FingerTree.Deep(
//                                left = Digit.One(left.first),
//                                middle = FingerTree.Empty(),
//                                right = Digit.Two(left.second, left.third),
//                            )
//                            is Digit.Two<T> -> FingerTree.Deep(
//                                left = Digit.One(left.first),
//                                middle = FingerTree.Empty(),
//                                right = Digit.One(left.second),
//                            )
//                            is Digit.One<T> -> FingerTree.Single(left.first)
//                        }
//                    )
//                    is Branch.Branch2<T> -> Pair(
//                        right.first,
//                        copy(middle = restMiddle, right = Digit.Two(branch.first, branch.second)),
//                    )
//                    is Branch.Branch3<T> -> Pair(
//                        right.first,
//                        copy(middle = restMiddle, right = Digit.Three(branch.first, branch.second, branch.third)),
//                    )
//                }
//            }
//        }
//    }
//}
//
//// helper function to "chunk up" odds and ends into deeper branches
//fun <T : Any> Iterable<T>.toBranches(): List<Branch<T>> {
//    val chunks = this.chunked(2)
//    return when {
//        chunks.size <= 1 && chunks.flatten().isEmpty() -> error("Not enough items to convert to branches (0).")
//        chunks.size <= 1 && chunks.flatten().size == 1 -> error("Not enough items to convert to branches (1).")
//        chunks.last().size == 1 -> {
//            chunks.dropLast(2).map { Branch.Branch2(it[0], it[1]) } +
//                    listOf(chunks.takeLast(2).flatten().let { Branch.Branch3(it[0], it[1], it[2]) })
//        }
//        else -> chunks.map { Branch.Branch2(it[0], it[1]) }
//    }
//}
//
//fun <T : Any> concatWithMiddle(left: FingerTree<T>, items: List<T>, right: FingerTree<T>): FingerTree<T> {
//    return when {
//        left is FingerTree.Empty<T> -> when {
//            items.isEmpty() -> right
//            else -> concatWithMiddle(FingerTree.Empty(), items.drop(1), right).prepend(items.first())
//        }
//        left is FingerTree.Single<T> -> {
//            concatWithMiddle(FingerTree.Empty(), items, right).prepend(left.value)
//        }
//        right is FingerTree.Empty<T> -> when {
//            items.isEmpty() -> left
//            else -> concatWithMiddle(left, items.dropLast(1), FingerTree.Empty()).append(items.last())
//        }
//        right is FingerTree.Single<T> -> {
//            concatWithMiddle(left, items, FingerTree.Empty()).append(right.value)
//        }
//        left is FingerTree.Deep<T> && right is FingerTree.Deep<T> -> FingerTree.Deep(
//            left = left.left,
//            middle = concatWithMiddle(left.middle, (left.right + items + right.left).toBranches(), right.middle),
//            right = right.right,
//        )
//        else -> error("above is actually exhaustive, but need exception for IntelliSense, shouldn't get here")
//    }
//}
//
//fun <T : Any> concat(left: FingerTree<T>, right: FingerTree<T>) = concatWithMiddle(left, emptyList(), right)
//
//
//fun main() {
////    val tree = (1 .. 100).fold(FingerTree.Empty<Int>() as FingerTree<Int>) { tree, x -> tree.append(x) }
////    val steps = generateSequence(Pair(-1, tree)) { (_, tree) ->
////        val (value, remainder) = tree.viewRight()
////        println(value)
////        if (value != null) Pair(value, remainder) else null
////    }
////    val (_, finalTree) = steps.last()
////    println(finalTree)
//
//    val tree1 = (1 .. 100).fold(FingerTree.Empty<Int>() as FingerTree<Int>) { tree, x -> tree.append(x) }
//    val tree2 = (101 .. 200).fold(FingerTree.Empty<Int>() as FingerTree<Int>) { tree, x -> tree.append(x) }
//    val tree = concat(tree1, tree2)
//    println(tree.toList())
//}
