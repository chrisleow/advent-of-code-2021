import kotlin.math.abs

fun main() {

    fun parseInput(input: List<String>): List<Int> {
        val regex = "\\d+".toRegex()
        return input.flatMap { line -> regex.findAll(line).map { it.value.toInt()} }
    }

    // let's go for maximum efficiency :)
    fun getMinimumCost(lower: Int, upper: Int, calculateCost: (Int) -> Int): Int {
        val memoizedCosts = mutableMapOf<Int, Int>()
        fun cost(pos: Int) = memoizedCosts.getOrPut(pos) { calculateCost(pos) }

        // this is a convex function, looking for minimum can be done with binary search
        // assessing "slope" at (middle, middle + 1)
        tailrec fun search(left: Int, right: Int): Int = when {
            right - left <= 1 -> {
                if (cost(left) <= cost(right)) left else right
            }
            else -> {
                val middle = (left + right) / 2
                when {
                    cost(middle) < cost(middle + 1) -> search(left, middle)
                    cost(middle) > cost(middle + 1) -> search(middle + 1, right)
                    else -> error("I'm not built for this!")
                }
            }
        }

        return cost(search(lower, upper))
    }

    fun part1(input: List<String>): Int {
        val positions = parseInput(input)
        val minPos = positions.minOrNull() ?: error("Empty List")
        val maxPos = positions.maxOrNull() ?: error("Empty List")
        return getMinimumCost(minPos, maxPos) { pos ->
            positions.sumOf { abs(it - pos) }
        }
    }

    fun part2(input: List<String>): Int {
        val positions = parseInput(input)
        val minPos = positions.minOrNull() ?: error("Empty List")
        val maxPos = positions.maxOrNull() ?: error("Empty List")
        return getMinimumCost(minPos, maxPos) { pos ->
            positions.sumOf {
                val delta = abs(it - pos)
                delta * (delta + 1) / 2
            }
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day07_test")
    check(part1(testInput) == 37)
    check(part2(testInput) == 168)

    val input = readInput("Day07")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
