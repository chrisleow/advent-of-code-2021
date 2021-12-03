fun main() {

    fun getGamma(input: List<String>): Int =
        (0 until input.maxOf { it.length })
            .map { index ->
                val count1 = input.count { it[index] == '1' }
                val count0 = input.count { it[index] == '0' }
                if (count1 > count0) '1' else '0'
            }
            .fold(0) { gamma, bit -> (gamma * 2) + (if (bit == '1') 1 else 0) }

    fun getEpsilon(input: List<String>): Int =
        (0 until input.maxOf { it.length })
            .map { index ->
                val count1 = input.count { it[index] == '1' }
                val count0 = input.count { it[index] == '0' }
                if (count1 < count0) '1' else '0'
            }
            .fold(0) { gamma, bit -> (gamma * 2) + (if (bit == '1') 1 else 0) }

    fun getRating(input: List<String>, getCriteriaBit: (count1: Int, count0: Int) -> Char): Int {
        val generations = generateSequence(Pair(0, input)) { (index, numbers) ->
            if (numbers.size <= 1) {
                null        // termination condition
            } else {
                val count1 = numbers.count { it[index] == '1' }
                val count0 = numbers.count { it[index] == '0' }
                val criteriaBit = getCriteriaBit(count1, count0)
                Pair(index + 1, numbers.filter { it[index] == criteriaBit })
            }
        }

        val finalNumber = generations.map { it.second }.last().first()
        return finalNumber.fold(0) { rating, bit ->
            (2 * rating) + (if (bit == '1') 1 else 0)
        }
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
