data class Cuboid(val xRange: IntRange, val yRange: IntRange, val zRange: IntRange) {
    fun isEmpty() = (xRange.isEmpty() || yRange.isEmpty() || zRange.isEmpty())
    val volume = listOf(xRange, yRange, zRange).fold(1L) { product, range ->
        if (range.isEmpty()) 0L else product * (range.last - range.first + 1)
    }
}

sealed class Instruction {
    abstract val cuboid: Cuboid
    data class On(override val cuboid: Cuboid) : Instruction()
    data class Off(override val cuboid: Cuboid) : Instruction()
}

data class State(val cuboids: List<Cuboid>, val antiCuboids: List<Cuboid>)

fun main() {

    fun parseInput(input: List<String>): List<Instruction> {
        val regex = "(on|off) x=(-?\\d+)\\.\\.(-?\\d+),y=(-?\\d+)\\.\\.(-?\\d+),z=(-?\\d+)\\.\\.(-?\\d+)".toRegex()
        return input
            .mapNotNull { regex.matchEntire(it.trim())?.groupValues }
            .map { gv ->
                val cuboid = Cuboid(
                    xRange = (gv[2].toInt() .. gv[3].toInt()),
                    yRange = (gv[4].toInt() .. gv[5].toInt()),
                    zRange = (gv[6].toInt() .. gv[7].toInt()),
                )
                if (gv[1] == "on") Instruction.On(cuboid) else Instruction.Off(cuboid)
            }
    }

    fun IntRange.intersect(other: IntRange) =
        (maxOf(this.first, other.first) .. minOf(this.last, other.last))

    fun Cuboid.intersect(other: Cuboid) = Cuboid(
        xRange = this.xRange.intersect(other.xRange),
        yRange = this.yRange.intersect(other.yRange),
        zRange = this.zRange.intersect(other.zRange),
    )

    fun State.apply(instruction: Instruction): State {
        return when (instruction) {
            is Instruction.On -> {
                val newCuboids = this.antiCuboids
                    .map { it.intersect(instruction.cuboid) }
                    .filter { !it.isEmpty() }
                val newAntiCuboids = this.cuboids
                    .map { it.intersect(instruction.cuboid) }
                    .filter { !it.isEmpty() }
                State(cuboids + newCuboids + instruction.cuboid, antiCuboids + newAntiCuboids)
            }
            is Instruction.Off -> {
                val newAntiCuboids = this.cuboids
                    .map { it.intersect(instruction.cuboid) }
                    .filter { !it.isEmpty() }
                val newCuboids = this.antiCuboids
                    .map { it.intersect(instruction.cuboid) }
                    .filter { !it.isEmpty() }
                State(cuboids + newCuboids, antiCuboids + newAntiCuboids)
            }
        }
    }

    fun part1(input: List<String>): Long {
        val instructions = parseInput(input)
            .takeWhile { instruction ->
                listOf(instruction.cuboid.xRange, instruction.cuboid.yRange, instruction.cuboid.zRange).all {
                    it.first >= -50 && it.last <= 50
                }
            }
        val initialState = State(emptyList(), emptyList())
        val finalState = instructions.fold(initialState) { state, instruction -> state.apply(instruction) }
        return finalState.cuboids.sumOf { it.volume } - finalState.antiCuboids.sumOf { it.volume }
    }

    fun part2(input: List<String>): Long {
        val instructions = parseInput(input)
        val initialState = State(emptyList(), emptyList())
        val finalState = instructions.fold(initialState) { state, instruction -> state.apply(instruction) }
        return finalState.cuboids.sumOf { it.volume } - finalState.antiCuboids.sumOf { it.volume }
    }

    // test
    val testInput = readInput("Day22_test")
    check(part1(testInput) == 474140L)
    check(part2(testInput) == 2758514936282235L)

    val input = readInput("Day22")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
