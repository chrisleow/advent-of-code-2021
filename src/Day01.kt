fun main() {
    fun part1(input: List<String>): Int {
        val readings = input.map { it.toInt() }
        val deltas = readings
            .zip(readings.drop(1))
            .map { (a, b) -> b - a }
        return deltas.count { it > 0 }
    }

    fun part2(input: List<String>): Int {
        val readings = input.map { it.toInt() }
        val windows = (0 .. readings.size - 3)
            .map { readings.slice(it until it + 3).sum() }
        val deltas = windows
            .zip(windows.drop(1))
            .map { (a, b)-> b - a }
        return deltas.count { it > 0 }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day01_test")
    check(part1(testInput) == 7)
    check(part2(testInput) == 5)

    val input = readInput("Day01")
    println(part1(input))
    println(part2(input))
}
