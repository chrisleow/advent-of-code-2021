fun main() {

    fun parseInput(input: List<String>): Map<Int, Long> {
        val pattern = "\\d+".toRegex()
        val numbers = input.flatMap { line -> pattern.findAll(line).map { it.value.toInt() } }
        return numbers
            .groupingBy { it }
            .eachCount()
            .mapValues { (_, c) -> c.toLong() }
    }

    fun Map<Int, Long>.next(): Map<Int, Long> {
        return this
            .flatMap { (timer, count) ->
                if (timer == 0) {
                    listOf(Pair(6, count), Pair(8, count))
                } else {
                    listOf(Pair(timer - 1, count))
                }
            }
            .groupingBy { it.first }
            .aggregate { _, count, timerCountPair, _ -> (count ?: 0L) + timerCountPair.second }
    }

    fun part1(input: List<String>): Long {
        val initialState = parseInput(input)
        val finalState = generateSequence(initialState) { it.next() }
            .drop(80)
            .first()
        return finalState.values.sum()
    }

    fun part2(input: List<String>): Long {
        val initialState = parseInput(input)
        val finalState = generateSequence(initialState) { it.next() }
            .drop(256)
            .first()
        return finalState.values.sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day06_test")
    check(part1(testInput) == 5934L)
    check(part2(testInput) == 26984457539L)

    val input = readInput("Day06")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
