fun main() {

    val digits = listOf(
        0 to "ABCEFG".toSet(),
        1 to "CF".toSet(),
        2 to "ACDEG".toSet(),
        3 to "ACDFG".toSet(),
        4 to "BCDF".toSet(),
        5 to "ABDFG".toSet(),
        6 to "ABDEFG".toSet(),
        7 to "ACF".toSet(),
        8 to "ABCDEFG".toSet(),
        9 to "ABCDFG".toSet(),
    )

    data class InputLine(val signalPatterns: List<String>, val outputPatterns: List<String>)

    fun parseInput(input: List<String>): List<InputLine> = input
        .filter { it.isNotBlank() && "|" in it }
        .map { line ->
            line.split("|")
                .map { section -> section.split(" ").filter { it.isNotBlank() } }
                .let { InputLine(it[0], it[1]) }
        }

    fun decodeOutput(line: InputLine): Int {
        val scrambledPatterns = line.signalPatterns + line.outputPatterns
        val digitPatternsByLength = digits.map { it.second }.groupBy { it.size }

        // boil down set of possibilities assuming a digit pattern to an output pattern
        fun Map<Char, Set<Char>>.restrict(scrambledChars: Set<Char>, realChars: Set<Char>) =
            this.mapValues { (scrambledChar, existingRealChars) ->
                when (scrambledChar in scrambledChars) {
                    true -> existingRealChars intersect realChars
                    false -> existingRealChars - realChars
                }
            }

        // search recursively by testing hypotheses for each digit (and restricting the mapping
        // of possibilities as we go)
        fun search(possibilities: Map<Char, Set<Char>>, index: Int): Map<Char, Char>? = when {
            possibilities.any { it.value.isEmpty() } -> null
            index >= scrambledPatterns.size -> possibilities.mapValues { it.value.single() }
            else -> {
                val scrambledPattern = scrambledPatterns[index].toSet()
                digitPatternsByLength[scrambledPattern.size]?.firstNotNullOfOrNull {
                    search(possibilities.restrict(scrambledPattern, it), index + 1)
                }
            }
        }

        // start search at the beginning with all possibilities
        val mapping = search("abcdefg".associateWith { "ABCDEFG".toSet() }, 0)
            ?: error("Should have a final mapping")

        return line.outputPatterns
            .map { pattern ->
                val realPattern = pattern.mapNotNull { mapping[it] }.toSet()
                digits.first { it.second == realPattern }.first
            }
            .fold(0) { acc, digit -> (acc * 10) + digit }
    }

    fun part1(input: List<String>): Int {
        val inputLines = parseInput(input)
        val lengths = digits
            .filter { it.first in listOf(1, 4, 7, 8) }
            .map { it.second.size }
        return inputLines
            .flatMap { it.outputPatterns }
            .count { it.length in lengths }
    }

    fun part2(input: List<String>): Int {
        val inputLines = parseInput(input)
        return inputLines.sumOf { decodeOutput(it) }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day08_test")
    check(part1(testInput) == 26)
    check(part2(testInput) == 61229)

    val input = readInput("Day08")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
