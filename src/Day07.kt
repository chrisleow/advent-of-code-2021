import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun main() {

    fun parseInput(input: List<String>): List<Int> = input
        .joinToString(",")
        .split(",")
        .filter { it.isNotBlank() }
        .map { it.toInt() }

    fun getMinMaxPositions(positions: List<Int>): Pair<Int, Int> = positions
        .fold(Pair(Int.MAX_VALUE, Int.MIN_VALUE)) { minMax, pos ->
            Pair(min(minMax.first, pos), max(minMax.second, pos))
        }

    fun part1(input: List<String>): Int {
        val positions = parseInput(input)
        val (minPos, maxPos) = getMinMaxPositions(positions)
        return (minPos .. maxPos).minOf {
            positions.sumOf { pos -> abs(it - pos) }
        }
    }

    fun part2(input: List<String>): Int {
        val positions = parseInput(input)
        val (minPos, maxPos) = getMinMaxPositions(positions)
        return (minPos .. maxPos).minOf {
            positions.sumOf { pos ->
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
