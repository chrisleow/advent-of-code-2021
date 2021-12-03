fun main() {

    fun String.bitsToInt(): Int =
        this.fold(0) { value, bit ->
            when (bit) {
                '1' -> (value shl 1) + 1
                '0' -> value shl 1
                else -> error("Bits should only be '1' or '0'.")
            }
        }

    fun getGamma(input: List<String>): Int =
        (0 until input.maxOf { it.length })
            .joinToString("") { index ->
                val count1 = input.count { it[index] == '1' }
                val count0 = input.count { it[index] == '0' }
                if (count1 > count0) "1" else "0"
            }
            .bitsToInt()

    fun getEpsilon(input: List<String>): Int =
        (0 until input.maxOf { it.length })
            .joinToString("") { index ->
                val count1 = input.count { it[index] == '1' }
                val count0 = input.count { it[index] == '0' }
                if (count1 < count0) "1" else "0"
            }
            .bitsToInt()

    fun getRating(input: List<String>, getCriteriaBit: (count1: Int, count0: Int) -> Char): Int {
        tailrec fun getNumber(index: Int, numbers: List<String>): String {
            when (numbers.size) {
                0 -> error("Should never get to 0 numbers.")
                1 -> return numbers.first()
            }

            // filter and move on one index recursively
            val count1 = numbers.count { it[index] == '1' }
            val count0 = numbers.count { it[index] == '0' }
            val criteriaBit = getCriteriaBit(count1, count0)
            return getNumber(index + 1, numbers.filter { it[index] == criteriaBit })
        }

        return getNumber(0, input).bitsToInt()
    }

    fun part1(input: List<String>): Int {
        val cleanInput = input.filter { it.isNotBlank() }.map { it.trim() }
        val gamma = getGamma(cleanInput)
        val epsilon = getEpsilon(cleanInput)
        return gamma * epsilon
    }

    fun part2(input: List<String>): Int {
        val cleanInput = input.filter { it.isNotBlank() }.map { it.trim() }
        val oxygenRating = getRating(cleanInput) { c1, c0 -> if (c1 >= c0) '1' else '0' }
        val scrubberRating = getRating(cleanInput) { c1, c0 -> if (c1 < c0) '1' else '0' }
        return oxygenRating * scrubberRating
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    check(part1(testInput) == 198)
    check(part2(testInput) == 230)

    val input = readInput("Day03")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
