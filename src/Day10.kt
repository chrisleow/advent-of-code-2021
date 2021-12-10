fun main() {

    class IllegalCharException(val char: Char) : Exception("Got an illegal character '${char}'.")

    fun getAutoCompleteString(line: String): String {
        val opposites = mapOf('(' to ')', '[' to ']', '{' to '}', '<' to '>')

        tailrec fun getAutoCompleteString(index: Int, closeStack: List<Char>): String = when {
            index >= line.length -> closeStack.reversed().joinToString("")
            line[index] in "([{<" -> {
                val expectedCloseChar = opposites[line[index]]!!
                getAutoCompleteString(index + 1, closeStack + expectedCloseChar)
            }
            else -> {
                when (val closeChar = line[index]) {
                    closeStack.last() -> getAutoCompleteString(index + 1, closeStack.dropLast(1))
                    else -> throw IllegalCharException(closeChar)
                }
            }
        }

        return getAutoCompleteString(0, emptyList())
    }

    fun part1(input: List<String>): Long {
        return input
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                try {
                    getAutoCompleteString(line)
                    null
                }
                catch (ex: IllegalCharException) {
                    ex.char
                }
            }
            .sumOf {
                when (it) {
                    ')' -> 3L
                    ']' -> 57L
                    '}' -> 1197L
                    '>' -> 25137L
                    else -> 0L
                }
            }
    }

    fun part2(input: List<String>): Long {
        val charScores = mapOf(')' to 1, ']' to 2, '}' to 3, '>' to 4)
        return input
            .filter { it.isNotBlank() }
            .mapNotNull {
                try { getAutoCompleteString(it) }
                catch(ex: IllegalCharException) { null }
            }
            .map { chars ->
                chars.fold(0L) { score, char ->
                    (score * 5) + (charScores[char] ?: error("Shouldn't get here."))
                }
            }
            .let { allScores -> allScores.sorted()[allScores.size / 2] }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day10_test")
    check(part1(testInput) == 26397L)
    check(part2(testInput) == 288957L)

    val input = readInput("Day10")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
