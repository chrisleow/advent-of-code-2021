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

    fun decode(line: InputLine): Pair<Long, Long> {
        val scrambledPatterns = line.signalPatterns + line.outputPatterns
        val digitPatternsByLength = digits.keys.groupBy { it.length }

        // boil down set of "possibilities" assuming a digit pattern to an output pattern
        fun Map<Char, String>.restrict(from: String, to: String): Map<Char, String> {
            assert(from.length == to.length) {
                "restricted 'from' and 'to' sets are the same size"
            }
            return this.mapValues { (char, possibleChars) ->
                when (char in from) {
                    true -> possibleChars.filter { it in to }.toCharArray().joinToString("")
                    false -> possibleChars.filter { it !in to }.toCharArray().joinToString("")
                }
            }
        }

        // search by testing hypotheses for each digit (and restricting the map as we go)
        fun search(map: Map<Char, String>, index: Int): Map<Char, String>? = when {
            map.any { it.value.isEmpty() } -> null
            index >= scrambledPatterns.size -> map
            else -> {
                val scrambledPattern = scrambledPatterns[index]
                digitPatternsByLength[scrambledPattern.length]?.firstNotNullOfOrNull {
                    search(map.restrict(scrambledPattern, it), index + 1)
                }
            }
        }

        val map = search("abcdefg".associateWith { "ABCDEFG" }, 0)
            ?.mapValues { it.value.first() }
            ?: error("Should have a final mapping")

        fun String.unscrambleDigit(): Int {
            val realPattern = this
                .mapNotNull { map[it] }
                .sorted()
                .joinToString("")
            return digits[realPattern] ?: error("Should have a mapping")
        }

        val signal = line.signalPatterns
            .map { it.unscrambleDigit() }
            .fold(0L) { acc, d -> (acc * 10) + d }
        val output = line.outputPatterns
            .map { it.unscrambleDigit() }
            .fold(0L) { acc, d -> (acc * 10) + d }
        return Pair(signal, output)
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
        return inputLines.sumOf { decode(it).second.toInt() }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day08_test")
    check(part1(testInput) == 26)
    check(part2(testInput) == 61229)

    val input = readInput("Day08")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
