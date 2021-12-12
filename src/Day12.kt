fun main() {

    fun parseLinks(input: List<String>): Map<String, List<String>> {
        val regex = "(\\w+)-(\\w+)".toRegex()
        return input
            .mapNotNull { regex.matchEntire(it)?.groupValues }
            .flatMap { gv -> listOf(gv[1] to gv[2], gv[2] to gv[1]) }
            .filter { (left, right) -> right != "start" && left != "end" }
            .groupBy({ it.first }) { it.second }
    }

    fun countPaths(links: Map<String, List<String>>, smallCaveDoubleVisitAllowed: Boolean): Int {
        fun countPaths(current: String, visited: List<String>, smallVisitAllowed: Boolean): Int =
            when (current == "end") {
                true -> 1
                false -> (links[current]?.asSequence() ?: emptySequence())
                    .filter { node -> smallVisitAllowed || node == node.uppercase() || node !in visited }
                    .sumOf { node ->
                        val smallVisitStillAllowed = smallVisitAllowed &&
                                (node !in visited || node == node.uppercase())
                        countPaths(node, visited + current, smallVisitStillAllowed)
                    }
            }
        return countPaths("start", emptyList(), smallCaveDoubleVisitAllowed)
    }

    fun part1(input: List<String>) = countPaths(parseLinks(input), false)
    fun part2(input: List<String>) = countPaths(parseLinks(input), true)

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day12_test")
    check(part1(testInput) == 226)
    check(part2(testInput) == 3509)

    val input = readInput("Day12")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
