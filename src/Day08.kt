fun main() {

    val digits = mapOf(
        "ABCEFG" to 0,
        "CF" to 1,
        "ACDEG" to 2,
        "ACDFG" to 3,
        "BCDF" to 4,
        "ABDFG" to 5,
        "ABDEFG" to 6,
        "ACF" to 7,
        "ABCDEFG" to 8,
        "ABCDFG" to 9,
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
        val digitPatternsByLength = digits.keys.groupBy { it.length }

        // boil down set of "possibilities" assuming a digit pattern to an output pattern
        fun Map<Char, Set<Char>>.restrict(scrambled: String, real: String): Map<Char, Set<Char>> {
            assert(scrambled.length == real.length) {
                "restricted 'scrambled' and 'real' strings are the same size"
            }
            return this.mapValues { (char, possibleChars) ->
                when (char in scrambled) {
                    true -> possibleChars intersect real.toSet()
                    false -> possibleChars - real.toSet()
                }
            }
        }

        // search by testing hypotheses for each digit (and restricting the map as we go)
        fun search(mapping: Map<Char, Set<Char>>, index: Int): Map<Char, Char>? = when {
            mapping.any { it.value.isEmpty() } -> null
            index >= scrambledPatterns.size -> mapping.mapValues { it.value.first() }
            else -> {
                val scrambledPattern = scrambledPatterns[index]
                digitPatternsByLength[scrambledPattern.length]?.firstNotNullOfOrNull {
                    search(mapping.restrict(scrambledPattern, it), index + 1)
                }
            }
        }

        val mapping = search("abcdefg".associateWith { "ABCDEFG".toSet() }, 0)
            ?: error("Should have a final mapping")

        return line.outputPatterns
            .map { pattern ->
                val realPattern = pattern
                    .mapNotNull { mapping[it] }
                    .sorted()
                    .joinToString("")
                digits[realPattern] ?: error("Should have a digit pattern for: $realPattern")
            }
            .fold(0) { acc, d -> (acc * 10) + d }
    }

    fun part1(input: List<String>): Int {
        val inputLines = parseInput(input)
        val reverseDigits = digits.asSequence().associate { Pair(it.value, it.key) }
        val lengths = listOf(1, 4, 7, 8).map { reverseDigits[it]?.length ?: 0 }
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
