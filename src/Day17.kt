import kotlin.math.abs

fun main() {

    data class TargetArea(val xRange: IntRange, val yRange: IntRange)
    data class State(val dx: Int, val dy: Int, val x: Int, val y: Int, val hit: Boolean)

    fun parseInput(input: List<String>): TargetArea {
        val regex = "target area: x=(\\-?\\d+)..(\\-?\\d+), y=(\\-?\\d+)..(\\-?\\d+)".toRegex()
        return regex.matchEntire(input.joinToString("").trim())
            ?.groupValues
            ?.let { gv -> TargetArea((gv[1].toInt() .. gv[2].toInt()), (gv[3].toInt() .. gv[4].toInt())) }
            ?: error("Input does not match pattern")
    }

    fun TargetArea.simulate(dx: Int, dy: Int): Sequence<State> {
        val floor = minOf(yRange.first, yRange.last)
        val wall = maxOf(xRange.first, xRange.last)
        return generateSequence(State(dx, dy, 0, 0, false)) { state ->
            if (state.hit || state.y < floor || state.x > wall) {
                null
            } else {
                val hit = ((state.x + state.dx) in xRange) && ((state.y + state.dy) in yRange)
                State(maxOf(state.dx - 1, 0), state.dy - 1, state.x + state.dx, state.y + state.dy, hit)
            }
        }
    }

    fun TargetArea.isHit(dx: Int, dy: Int) = this.simulate(dx, dy).any { it.hit }

    // looking for any possible X that could hit ...
    fun TargetArea.getPossibleHorizontalVelocities(): List<Int> {
        val weakestX = (0 .. xRange.last).first { it * (it + 1) / 2 in xRange}
        return (weakestX .. xRange.last).toList()
    }

    // look for any y that could possibly hit
    // note, for any impact, there are two possible ways to get there, up and over
    // or straight down
    fun TargetArea.getPossibleVerticalVelocities(): List<Int> {
        val floor = minOf(yRange.first, yRange.last)
        return (0 downTo minOf(yRange.first, yRange.last))
            .filter { initialDy ->
                val velocityPositions = generateSequence(Pair(initialDy, 0)) { (dy, y) ->
                    if (y < floor) null else Pair(dy - 1, y + dy)
                }
                velocityPositions.any { (_, y) -> y in yRange }
            }
            .flatMap { dy -> listOf(dy, 1 - dy) }
            .distinct()
    }

    // always assume we can pick an x to "come to rest" inside the target area
    // also, the projectile always passes "0" as it comes down, so we track up from the lowest
    // "y" point and get the triangle number to extrapolate highest "y".
    fun part1(input: List<String>): Int {
        val targetArea = parseInput(input)
        val largestYGap = abs(minOf(targetArea.yRange.first, targetArea.yRange.last))
        return largestYGap * (largestYGap - 1) / 2
    }

    fun part2(input: List<String>): Int {
        val targetArea = parseInput(input)
        val allDx = targetArea.getPossibleHorizontalVelocities()
        val allDy = targetArea.getPossibleVerticalVelocities()
        return allDx.sumOf { dx -> allDy.count { dy -> targetArea.isHit(dx, dy) } }
    }

    // test
    val testInput = readInput("Day17_test")
    check(part1(testInput) == 45)
    check(part2(testInput) == 112)

    val input = readInput("Day17")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
