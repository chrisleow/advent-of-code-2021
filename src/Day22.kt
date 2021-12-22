data class Cuboid(val xRange: IntRange, val yRange: IntRange, val zRange: IntRange) {
    val volume = listOf(xRange, yRange, zRange).fold(1L) { product, range ->
        if (range.isEmpty()) 0L else product * (range.last - range.first + 1)
    }
}

sealed class Instruction {
    abstract val cuboid: Cuboid
    data class On(override val cuboid: Cuboid) : Instruction()
    data class Off(override val cuboid: Cuboid) : Instruction()
}

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

    // partition a range into (left-remainder, intersection, right-remainder), one of these may be empty
    // need to deal with several cases here ...
    fun IntRange.partitionBy(other: IntRange): Pair<IntRange, List<IntRange>> {
        return when {
            other.first in this && other.last in this -> {
                Pair((other.first .. other.last),
                    listOf((this.first until other.first), (other.last + 1 .. this.last)))
            }
            other.first in this -> {
                Pair((other.first .. this.last), listOf(this.first until other.first))
            }
            other.last in this -> {
                Pair((this.first .. other.last), listOf(other.last + 1 .. this.last))
            }
            other.first < this.first && other.last > this.last -> {
                Pair(this, emptyList())
            }
            else -> {
                Pair(IntRange.EMPTY, listOf(this))
            }
        }
    }

    // removes any intersection with given cuboid
    fun Cuboid.remove(other: Cuboid): List<Cuboid> {
        val (xIntersect, xRemainders) = this.xRange.partitionBy(other.xRange)
        val (yIntersect, yRemainders) = this.yRange.partitionBy(other.yRange)
        val (zIntersect, zRemainders) = this.zRange.partitionBy(other.zRange)

        // give back all cuboids except the intersection cuboid
        val intersectCuboid = Cuboid(xIntersect, yIntersect, zIntersect)
        return (xRemainders + listOf(xIntersect)).flatMap { xRange ->
            (yRemainders + listOf(yIntersect)).flatMap { yRange ->
                (zRemainders + listOf(zIntersect))
                    .map { zRange -> Cuboid(xRange, yRange, zRange) }
                    .filter { it != intersectCuboid && it.volume > 0 }
            }
        }
    }

    fun List<IntRange>.reduce(): List<IntRange> {
        if (this.isEmpty()) {
            return emptyList()
        }

        val sortedRanges = this.sortedBy { it.first }
        val initialRange = sortedRanges.first()
        val initialState: Triple<List<IntRange>, Int, Int> = Triple(emptyList(), initialRange.first, initialRange.last)
        val (ranges, first, last) = sortedRanges
            .drop(1)
            .fold(initialState) { (ranges, first, last), range ->
                if (last + 1 == range.first) {
                    Triple(ranges, first, range.last)
                } else {
                    Triple(ranges + listOf(first .. last), range.first, range.last)
                }
            }
        return ranges + listOf(first .. last)
    }

    // merge adjacent cuboids where possible
    fun List<Cuboid>.reduce() = this
        .groupBy { cuboid -> Pair(cuboid.xRange, cuboid.yRange) }
        .flatMap { (xyRanges, cuboids) ->
            val (xRange, yRange) = xyRanges
            cuboids.map { it.zRange }.reduce().map { zRange -> Cuboid(xRange, yRange, zRange) }
        }
//        .groupBy { cuboid -> Pair(cuboid.xRange, cuboid.zRange) }
//        .flatMap { (xzRanges, cuboids) ->
//            val (xRange, zRange) = xzRanges
//            cuboids.map { it.yRange }.reduce().map { yRange -> Cuboid(xRange, yRange, zRange) }
//        }
//        .groupBy { cuboid -> Pair(cuboid.yRange, cuboid.zRange) }
//        .flatMap { (yzRanges, cuboids) ->
//            val (yRange, zRange) = yzRanges
//            cuboids.map { it.xRange }.reduce().map { xRange -> Cuboid(xRange, yRange, zRange) }
//        }

    fun List<Cuboid>.apply(instruction: Instruction): List<Cuboid> = this
        .flatMap { cuboid -> cuboid.remove(instruction.cuboid) }
        .plus(if (instruction is Instruction.On) listOf(instruction.cuboid) else emptyList())
        .reduce()

    fun part1(input: List<String>): Long {
        val instructions = parseInput(input)
            .takeWhile { instruction ->
                listOf(instruction.cuboid.xRange, instruction.cuboid.yRange, instruction.cuboid.zRange).all {
                    it.first >= -50 && it.last <= 50
                }
            }
        return instructions
            .fold(emptyList<Cuboid>()) { cuboids, instruction -> cuboids.apply(instruction) }
            .sumOf { cuboid -> cuboid.volume }
    }

    fun part2(input: List<String>): Long {
        val instructions = parseInput(input)
        return instructions
            .fold(emptyList<Cuboid>()) { cuboids, instruction -> cuboids.apply(instruction) }
            .sumOf { cuboid -> cuboid.volume }
    }

    // test
    val testInput = readInput("Day22_test")
    check(part1(testInput) == 474140L)
    check(part2(testInput) == 2758514936282235L)

    val input = readInput("Day22")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
