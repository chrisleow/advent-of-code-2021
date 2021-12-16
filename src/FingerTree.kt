//sealed class FingerTree<T> {
//    class Empty<T> : FingerTree<T>()
//    data class Single<T>(val value: T) : FingerTree<T>()
//    data class Deep<T>(val left: Digit<T>, val middle: FingerTree<Branch<T>>, val right: Digit<T>) : FingerTree<T>()
//
//    val size: Int = run {
//        when (this) {
//            is Empty<*> -> 0
//            is Single<*> -> if (value is Branch<*>) value.size else 1
//            is Deep<*> -> left.size + middle.size + right.size
//        }
//    }
//}
//
//sealed class Branch<T> {
//    data class Pair<T>(val first: T, val second: T) : Branch<T>()
//    data class Triple<T>(val first: T, val second: T, val third: T) : Branch<T>()
//
//    val size: Int = run {
//        val elements = when (this) {
//            is Pair<*> -> sequenceOf(first, second)
//            is Triple<*> -> sequenceOf(first, second, third)
//        }
//        elements.sumOf { if (it is Branch<*>) it.size else 1 }
//    }
//}
//
//sealed class Digit<T> {
//    data class One<T>(val first: T) : Digit<T>()
//    data class Two<T>(val first: T, val second: T) : Digit<T>()
//    data class Three<T>(val first: T, val second: T, val third: T) : Digit<T>()
//    data class Four<T>(val first: T, val second: T, val third: T, val fourth: T) : Digit<T>()
//
//    val size: Int = run {
//        val elements = when (this) {
//            is One<*> -> sequenceOf(first)
//            is Two<*> -> sequenceOf(first, second)
//            is Three<*> -> sequenceOf(first, second, third)
//            is Four<*> -> sequenceOf(first, second, third, fourth)
//        }
//        elements.sumOf { if (it is Branch<*>) it.size else 1 }
//    }
//}
//
//fun <T> FingerTree<T>.addLeft(item: T): FingerTree<T> = when (this) {
//    is FingerTree.Empty<*> -> FingerTree.Single(item)
//    is FingerTree.Single<*> -> FingerTree.Deep(Digit.One(item), FingerTree.Empty(), Digit.One(value))
//    is FingerTree.Deep<*> -> TODO("DO this")
//}
