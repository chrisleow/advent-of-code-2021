fun main() {

    data class Point(val x: Int, val y: Int)

    fun parseInput(input: List<String>): Map<Point, Int> = buildMap {
        input.filter { it.isNotBlank() }.forEachIndexed { y, line ->
            line.trim().forEachIndexed { x, char ->
                put(Point(x, y), char.toString().toInt())
            }
        }
    }

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
        tailrec fun expand(edge: Set<Point>, basin: Set<Point> = emptySet()): Set<Point> {
            val nextEdge = edge
                .flatMap { it.getAdjacent() }
                .filter { it !in basin && it !in edge && (heightMap[it] ?: 10) < 9 }
                .toSet()
            return when {
                nextEdge.isEmpty() -> basin + edge
                else -> expand(nextEdge, basin + edge)
            }
        }

        return getLowPoints(heightMap).map { expand(setOf(it)).toList() }
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
            .fold(1, Int::times)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day09_test")
    check(part1(testInput) == 15)
    check(part2(testInput) == 1134)

    val input = readInput("Day09")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
