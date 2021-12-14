fun main() {

    data class CharPair(val left: Char, val right: Char, val isStart: Boolean, val isEnd: Boolean)

    fun parseInput(input: List<String>): Pair<String, Map<Pair<Char, Char>, Char>> {
        val regex = "(\\w)(\\w)\\s*->\\s*(\\w)".toRegex()
        val initialString = input.first().trim()
        val rules = input
            .mapNotNull { line -> regex.matchEntire(line)?.groupValues }
            .associate { gv -> Pair(gv[1].single(), gv[2].single()) to gv[3].single() }
        return Pair(initialString, rules)
    }

    fun Map<CharPair, Long>.next(ruleMap: Map<Pair<Char, Char>, Char>): Map<CharPair, Long> {
        return this.entries
            .flatMap { entry ->
                when (val insertChar = ruleMap[Pair(entry.key.left, entry.key.right)]) {
                    null -> listOf(
                        Pair(entry.key, entry.value),
                    )
                    else -> listOf(
                        Pair(entry.key.copy(right = insertChar, isEnd = false), entry.value),
                        Pair(entry.key.copy(left = insertChar, isStart = false), entry.value),
                    )
                }
            }
            .groupBy({ it.first }) { it.second }
            .mapValues { it.value.sum() }
    }

    fun getElementDifference(input: List<String>, steps: Int): Long {
        val (initialString, ruleMap) = parseInput(input)
        val initialMap = initialString
            .zipWithNext()
            .withIndex()
            .map { (index, pair) ->
                CharPair(
                    left = pair.first,
                    right = pair.second,
                    isStart = (index == 0),
                    isEnd = (index == initialString.length - 2),
                )
            }
            .groupingBy { it }
            .eachCount()
            .mapValues { it.value.toLong() }

        val finalMap = generateSequence(initialMap) { it.next(ruleMap) }
            .drop(steps)
            .first()
        val elementCounts = finalMap
            .flatMap { (charPair, count) ->
                listOf(
                    Pair(charPair.left, if (charPair.isStart) count * 2 else count),
                    Pair(charPair.right, if (charPair.isEnd) count * 2 else count),
                )
            }
            .groupBy({ it.first }) { it.second }
            .mapValues { it.value.sum() / 2 }
        return elementCounts.maxOf { it.value } - elementCounts.minOf { it.value }
    }

    fun part1(input: List<String>): Long = getElementDifference(input, 10)
    fun part2(input: List<String>): Long = getElementDifference(input, 40)

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day14_test")
    check(part1(testInput) == 1588L)
    check(part2(testInput) == 2188189693529L)

    val input = readInput("Day14")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
