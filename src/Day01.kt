fun main() {
    fun part1(input: List<String>): Int {
        val readings = input.map { it.toInt() }
        return (0 until readings.size - 1).count {  readings[it + 1] > readings[it] }
    }

    fun part2(input: List<String>): Int {
        val readings = input.map { it.toInt() }
        val windows = (0 .. readings.size - 3).map { readings.slice(it until it + 3).sum() }
        return (0 until windows.size - 1).count { windows[it + 1] > windows[it] }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day01_test")
    check(part1(testInput) == 7)
    check(part2(testInput) == 5)

    val input = readInput("Day01")
    println(part1(input))
    println(part2(input))
}
