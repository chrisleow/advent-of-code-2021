import java.util.LinkedList
import java.util.Queue

fun main() {

    data class Point(val x: Int, val y: Int)

    fun parseInput(input: List<String>): Map<Point, Int> = input
        .filter { it.isNotBlank() }
        .flatMapIndexed { y, line ->
            line.trim().mapIndexed { x, char ->
                Point(x, y) to char.toString().toInt()
            }
        }
        .toMap()

    fun Point.getAdjacent() = listOf(
        Point(x, y + 1),
        Point(x, y - 1),
        Point(x + 1, y),
        Point(x - 1, y),
    )

    fun getLowPoints(heightMap: Map<Point, Int>): List<Point> {
        val maxX = heightMap.keys.maxOf { it.x }
        val maxY = heightMap.keys.maxOf { it.y }

        return (0 .. maxX).flatMap { x ->
            (0 .. maxY).mapNotNull { y ->
                val point = Point(x, y)
                val height = heightMap[point] ?: 10
                val adjacentHeights = point.getAdjacent().map { heightMap[it] ?: 10 }
                if (adjacentHeights.all { height < it }) point else null
            }
        }
    }

    fun getBasins(heightMap: Map<Point, Int>): List<List<Point>> {
        fun height(point: Point) = heightMap[point] ?: 10

        return getLowPoints(heightMap).map { lowPoint ->
            val queue: Queue<Point> = LinkedList(listOf(lowPoint))
            val basin = mutableSetOf<Point>()
            while (queue.isNotEmpty()) {
                val point = queue.remove()
                if (point !in basin && height(point) < 9) {
                    basin.add(point)
                    queue.addAll(point.getAdjacent())
                }
            }
            basin.toList()
        }
    }

    fun part1(input: List<String>): Int {
        val heightMap = parseInput(input)
        return getLowPoints(heightMap)
            .sumOf { (heightMap[it] ?: 0) + 1 }
    }

    fun part2(input: List<String>): Int {
        val heightMap = parseInput(input)
        return getBasins(heightMap)
            .map { it.size }
            .sortedByDescending { it }
            .take(3)
            .fold(1) { acc, size -> acc * size }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day09_test")
    check(part1(testInput) == 15)
    check(part2(testInput) == 1134)

    val input = readInput("Day09")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
