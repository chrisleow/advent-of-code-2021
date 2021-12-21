

class SemiHeapPriorityQueue<T> private constructor (
    private val root: Root<T>,
    private val comparator: Comparator<T>,
) {

    sealed class Root<T> {
        class Empty<T> : Root<T>()
        data class Winner<T>(val winner: T, val losers: LoserTree<T>) : Root<T>()
    }

    sealed class LoserTree<T> {
        class Empty<T> : LoserTree<T>()
        data class Node<T>(val item: T, val left: LoserTree<T>, val right: LoserTree<T>) : LoserTree<T>()
    }


}