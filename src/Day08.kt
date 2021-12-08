fun main() {

    val digits = mapOf(
        "ABCEFG".toSet() to 0,
        "CF".toSet() to 1,
        "ACDEG".toSet() to 2,
        "ACDFG".toSet() to 3,
        "BCDF".toSet() to 4,
        "ABDFG".toSet() to 5,
        "ABDEFG".toSet() to 6,
        "ACF".toSet() to 7,
        "ABCDEFG".toSet() to 8,
        "ABCDFG".toSet() to 9,
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
        val digitPatternsByLength = digits.keys.groupBy { it.size }

        // boil down set of possibilities assuming a digit pattern to an output pattern
        fun Map<Char, Set<Char>>.restrict(scrambled: Set<Char>, real: Set<Char>): Map<Char, Set<Char>> {
            assert(scrambled.size == real.size) {
                "restricted 'scrambled' and 'real' strings are the same size"
            }
            return this.mapValues { (scrambledChar, possibleRealChars) ->
                when (scrambledChar in scrambled) {
                    true -> possibleRealChars intersect real
                    false -> possibleRealChars - real
                }
            }
        }

        // search recursively by testing hypotheses for each digit (and restricting the mapping
        // of possibilities as we go)
        fun search(possibilities: Map<Char, Set<Char>>, index: Int): Map<Char, Char>? = when {
            possibilities.any { it.value.isEmpty() } -> null
            index >= scrambledPatterns.size -> possibilities.mapValues { it.value.first() }
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
                digits[realPattern] ?: error("Should have a digit pattern for: $realPattern")
            }
            .fold(0) { acc, d -> (acc * 10) + d }
    }

    fun part1(input: List<String>): Int {
        val inputLines = parseInput(input)
        val reverseDigits = digits.asSequence().associate { Pair(it.value, it.key) }
        val lengths = listOf(1, 4, 7, 8).map { reverseDigits[it]?.size ?: 0 }
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
