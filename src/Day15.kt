fun main() {

    data class Point(val x: Int, val y: Int) {
        val adjacent by lazy {
            listOf(
                Point(x - 1, y),
                Point(x, y - 1),
                Point(x + 1, y),
                Point(x, y + 1),
            )
        }
    }

    fun parseInput(input: List<String>): Map<Point, Int> = buildMap {
        input.filter { it.isNotBlank() }.forEachIndexed { y, line ->
            line.trim().forEachIndexed { x, char ->
                put(Point(x, y), char.toString().toInt())
            }
        }
    }

    fun expandMap(risks: Map<Point, Int>): Map<Point, Int> = buildMap {
        val maxX = risks.keys.maxOf { it.x }
        val maxY = risks.keys.maxOf { it.y }
        (0 .. 4).forEach { tileX ->
            (0 .. 4).forEach { tileY ->
                (0..maxX).forEach { dx ->
                    (0..maxY).forEach { dy ->
                        val point = Point(tileX * (maxX + 1) + dx, tileY * (maxY + 1) + dy)
                        val risk = when (val originalRisk = risks[Point(dx, dy)]) {
                            null -> error("should never get here")
                            else -> ((originalRisk + tileX + tileY - 1) % 9) + 1
                        }
                        put(point, risk)
                    }
                }
            }
        }
    }

    // Note, I have to break my "functional-only" rule at this point, as performance is a real issue ...
    // Using a bastardised version of Dijstra's algorithm
    fun getLowestCost(risks: Map<Point, Int>): Int {
        val maxX = risks.keys.maxOf { it.x }
        val maxY = risks.keys.maxOf { it.y }

        // working variables
        var edge = setOf(Point(0, 0))
        val costs = mutableMapOf(Point(0, 0) to 0)
        while (edge.isNotEmpty()) {
            val nextEdgeCosts = edge
                .asSequence()
                .flatMap { point -> point.adjacent }
                .filter { point -> point.x in (0 .. maxX) && point.y in (0 .. maxY) }
                .distinct()
                .map { point ->
                    val risk = risks[point] ?: error("no risk for point")
                    val cost = point.adjacent.minOf { (costs[it] ?: 1_000_000_000) + risk }
                    (point to cost)
                }
                .filter { (point, cost) -> cost < (costs[point] ?: 1_000_000_000) }
                .toMap()
            edge = nextEdgeCosts.keys
            costs.putAll(nextEdgeCosts)
        }

        // final point cost
        return costs[Point(maxX, maxY)] ?: error("shouldn't get here")
    }

    fun part1(input: List<String>): Int {
        val risks = parseInput(input)
        return getLowestCost(risks)
    }

    fun part2(input: List<String>): Int {
        val risks = expandMap(parseInput(input))
        return getLowestCost(risks)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day15_test")
    check(part1(testInput) == 40)
    check(part2(testInput) == 315)

    val input = readInput("Day15")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
