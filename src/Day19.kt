fun main() {

    data class Vector(val x: Int, val y: Int, val z: Int) {
        operator fun plus(other: Vector) = Vector(x + other.x, y + other.y, z + other.z)
        operator fun minus(other: Vector) = Vector(x - other.x, y - other.y, z - other.z)
    }

    data class VectorAlignment(val points: Set<Vector>, val displacement: Vector, val commonSize: Int)
    data class ScannerMap(val beacons: List<Vector>, val scannerDisplacements: List<Vector>)

    fun <T> Iterable<T>.split(delimiter: (T) -> Boolean): Iterable<Iterable<T>> = this
        .fold(listOf(emptyList<T>())) { acc, item ->
            when (delimiter(item)) {
                true -> acc + listOf(emptyList())
                false -> acc.dropLast(1) + listOf(acc.last() + item)
            }
        }
        .filter { it.isNotEmpty() }

    fun parseInput(input: List<String>): List<List<Vector>> {
        val regex = "(-?\\d+),(-?\\d+),(-?\\d+)".toRegex()
        return input
            .split { it.isBlank() }
            .map { lines ->
                lines
                    .mapNotNull { regex.matchEntire(it)?.groupValues }
                    .map { gv -> Vector(gv[1].toInt(), gv[2].toInt(), gv[3].toInt()) }
            }
    }

    fun List<Vector>.allOrientations(): List<List<Vector>> {
        val vectorOrientations = this.map { (x, y, z) ->
            val permutations = listOf(
                Vector(x, y, z),
                Vector(x, z, -y),
                Vector(y, x, -z),
                Vector(y, z, x),
                Vector(z, x, y),
                Vector(z, y, -x),
            )
            permutations.flatMap { (x, y, z) ->
                listOf(
                    Vector(x, y, z),
                    Vector(-x, -y, z),
                    Vector(x, -y, -z),
                    Vector(-x, y, -z),
                )
            }
        }
        return (0 until vectorOrientations.maxOf { it.size })
            .map { index -> vectorOrientations.map { it[index] } }
    }

    // try to align every point
    fun List<Vector>.bestAlignmentWith(other: Set<Vector>): VectorAlignment {
        val otherSet = other.toSet()
        return other
            .flatMap { targetPoint ->
                this.map { point ->
                    val displaced = this.map { it + targetPoint - point }.toSet()
                    VectorAlignment(displaced, targetPoint - point, (displaced intersect otherSet).size)
                }
            }
            .maxByOrNull { it.commonSize }
            ?: error("no empty lists please!")
    }

    fun List<List<Vector>>.getCompleteMap(): ScannerMap {

        // given a partial map and displacement, build the full map
        fun buildMap(remainingScans: List<List<Vector>>, alignments: List<VectorAlignment>): List<VectorAlignment> {
            if (remainingScans.isEmpty()) {
                return alignments
            }

            println("Remaining Scans: ${remainingScans.size}")
            val (winningIndex, alignment) = remainingScans.indices.firstNotNullOf { index ->
                val scan = remainingScans[index]
                scan.allOrientations()
                    .firstNotNullOfOrNull { orientedScan ->
                        alignments.firstNotNullOfOrNull { existingAlignment ->
                            val scanAlignment = orientedScan.bestAlignmentWith(existingAlignment.points)
                            when (scanAlignment.commonSize >= 12) {
                                true -> Pair(index, scanAlignment)
                                false -> null
                            }
                        }
                    }
            }

            val newRemainingScans = remainingScans
                .withIndex()
                .filter { (index, _) -> index != winningIndex }
                .map { (_, scan) -> scan }
            return buildMap(newRemainingScans, alignments + alignment)
        }

        // start with scanner 0 as the "gold standard"
        val initialAlignment = VectorAlignment(this.first().toSet(), Vector(0, 0, 0), -1)
        val alignments = buildMap(this.drop(1), listOf(initialAlignment))
        return ScannerMap(alignments.flatMap { it.points }.distinct(), alignments.map { it.displacement })
    }

    fun part1(input: List<String>): Int {
        return parseInput(input).getCompleteMap().beacons.size
    }

    fun part2(input: List<String>): Int {
        val displacements = parseInput(input).getCompleteMap().scannerDisplacements
        return displacements
            .flatMap { p1 -> displacements.map { p2 -> (p1 - p2).let { (x, y, z) -> x + y + z } } }
            .maxOrNull()
            ?: error("seriously?")
    }

    // test
    val testInput = readInput("Day19_test")
    check(part1(testInput) == 79)
    check(part2(testInput) == 3621)

    val input = readInput("Day19")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
