enum class Direction { FORWARD, DOWN, UP }

fun main() {

    fun readInstructions(input: List<String>): List<Pair<Direction, Int>> {
        val regex = "(\\w+)\\s+(\\d+)".toRegex()
        return input.mapNotNull {
            regex.matchEntire(it)?.let { match ->
                val direction = Direction.valueOf(match.groupValues[1].uppercase())
                val units = match.groupValues[2].toInt()
                Pair(direction, units)
            }
        }
    }

    fun part1(input: List<String>): Int {
        val instructions = readInstructions(input)

        data class State(val pos: Int, val depth: Int)

        val initialState = State(0, 0)
        val finalState = instructions.fold(initialState) { state, instruction ->
            val (direction, units) = instruction
            when (direction) {
                Direction.FORWARD -> state.copy(pos = state.pos + units)
                Direction.UP -> state.copy(depth = state.depth - units)
                Direction.DOWN -> state.copy(depth = state.depth + units)
            }
        }

        return finalState.pos * finalState.depth
    }

    fun part2(input: List<String>): Int {
        val instructions = readInstructions(input)

        data class State(val pos: Int, val depth: Int, val aim: Int)

        val initialState = State(0, 0, 0)
        val finalState = instructions.fold(initialState) { state, instruction ->
            val (direction, units) = instruction
            when (direction) {
                Direction.FORWARD -> state.copy(
                    pos = state.pos + units,
                    depth = state.depth + (state.aim * units),
                )
                Direction.UP -> state.copy(aim = state.aim - units)
                Direction.DOWN -> state.copy(aim = state.aim + units)
            }
        }

        return finalState.pos * finalState.depth
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test")
    check(part1(testInput) == 150)
    check(part2(testInput) == 900)

    val input = readInput("Day02")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
