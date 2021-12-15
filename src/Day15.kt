import java.util.PriorityQueue

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
    // Using an A* search algorithm
    fun getLowestCost(risks: Map<Point, Int>): Int {
        val maxX = risks.keys.maxOf { it.x }
        val maxY = risks.keys.maxOf { it.y }

        // A* queue indexed by (priority, cost, point)
        val minCostsSoFar = mutableMapOf(Point(0, 0) to 0)
        val queue = PriorityQueue<Pair<Int, Point>>(compareBy { it.first })
        queue.add(Pair(0, Point(0, 0)))

        // continue until destination achieved
        while (queue.isNotEmpty()) {
            val (_, point) = queue.remove()
            if (point == Point(maxX, maxY)) {
                break
            }

            // examine adjacent points as per A* / Dijkstra
            // note we use (-ManhattenDistance) as our distance heuristic
            val pointCost = minCostsSoFar[point] ?: continue
            point.adjacent
                .filter { it.x in (0 .. maxX) && it.y in (0 .. maxY) }
                .forEach { adjacentPoint ->
                    val potentialCost = pointCost + (risks[adjacentPoint] ?: 1_000_000_000)
                    if (potentialCost < (minCostsSoFar[adjacentPoint] ?: 1_000_000_000)) {
                        val priority = potentialCost - (adjacentPoint.x + adjacentPoint.y)
                        minCostsSoFar[adjacentPoint] = potentialCost
                        queue.add(priority to adjacentPoint)
                    }
                }
        }

        return minCostsSoFar[Point(maxX, maxY)] ?: error("shouldn't get here")
    }

    fun part1(input: List<String>): Int = getLowestCost(parseInput(input))
    fun part2(input: List<String>): Int = getLowestCost(expandMap(parseInput(input)))

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day15_test")
    check(part1(testInput) == 40)
    check(part2(testInput) == 315)

    val input = readInput("Day15")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
