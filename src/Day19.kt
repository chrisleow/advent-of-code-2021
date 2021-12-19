import kotlin.math.abs

fun main() {

    data class Vector(val x: Int, val y: Int, val z: Int) {
        operator fun plus(other: Vector) = Vector(x + other.x, y + other.y, z + other.z)
        operator fun minus(other: Vector) = Vector(x - other.x, y - other.y, z - other.z)
    }

    data class OrientedScan(
        val beacons: Set<Vector>,
        val translationNorms: Set<Int>,
        val absolutePosition: Vector,
    )

    fun Vector.norm() = abs(x) + abs(y) + abs(z)

    fun <T> Iterable<T>.split(delimiter: (T) -> Boolean): Iterable<Iterable<T>> = this
        .fold(listOf(emptyList<T>())) { acc, item ->
            when (delimiter(item)) {
                true -> acc + listOf(emptyList())
                false -> acc.dropLast(1) + listOf(acc.last() + item)
            }
        }
        .filter { it.isNotEmpty() }

    fun parseInput(input: List<String>): List<OrientedScan> {
        val regex = "(-?\\d+),(-?\\d+),(-?\\d+)".toRegex()
        return input
            .split { it.isBlank() }
            .map { lines ->
                val beacons = lines
                    .mapNotNull { regex.matchEntire(it)?.groupValues }
                    .map { gv -> Vector(gv[1].toInt(), gv[2].toInt(), gv[3].toInt()) }
                OrientedScan(
                    beacons = beacons.toSet(),
                    absolutePosition = Vector(0, 0, 0),
                    translationNorms = beacons
                        .flatMap { b1 -> beacons.map { b2 -> (b1 - b2).norm() } }
                        .filter { it > 0 }
                        .toSet(),
                )
            }
    }

    // careful to avoid flips (can't be arsed to do it properly with matrices)
    val orientationTransformers = run {
        val permutations = listOf<(Vector) -> Vector>(
            { (x, y, z) -> Vector(x, y, z) },
            { (x, y, z) -> Vector(x, z, -y) },
            { (x, y, z) -> Vector(y, x, -z) },
            { (x, y, z) -> Vector(y, z, x) },
            { (x, y, z) -> Vector(z, x, y) },
            { (x, y, z) -> Vector(z, y, -x) },
        )
        permutations.flatMap { transform ->
            listOf<(Vector) -> Vector>(
                { v -> transform(v).let { (x, y, z) -> Vector(x, y, z) } },
                { v -> transform(v).let { (x, y, z) -> Vector(-x, -y, z) } },
                { v -> transform(v).let { (x, y, z) -> Vector(x, -y, -z) } },
                { v -> transform(v).let { (x, y, z) -> Vector(-x, y, -z) } },
            )
        }
    }

    operator fun OrientedScan.plus(other: Vector) = OrientedScan(
        beacons = beacons.map { it + other }.toSet(),
        translationNorms = translationNorms,
        absolutePosition = absolutePosition + other,
    )

    fun OrientedScan.getAllOrientations(): Sequence<OrientedScan> = sequence {
        orientationTransformers.forEach { transform ->
            yield(
                OrientedScan(
                    beacons = beacons.map { transform(it) }.toSet(),
                    translationNorms = translationNorms,
                    absolutePosition = transform(absolutePosition),
                )
            )
        }
    }

    fun List<OrientedScan>.orientAll(): List<OrientedScan> {
        tailrec fun orientStep(remaining: List<OrientedScan>, known: List<OrientedScan>): List<OrientedScan> {
            if (remaining.isEmpty()) return known

            // look for first matching scan
            val (index, replacement) = remaining.indices.firstNotNullOf { index ->
                val scan = remaining[index]
                known.firstNotNullOfOrNull { knownScan ->
                    val intersectingNorms = knownScan.translationNorms intersect scan.translationNorms
                    if (intersectingNorms.size < 66) {

                        // expecting min. 66 translations where the norm value is the same, irrespective of orientation
                        null
                    } else {

                        // now have to work harder, examine mutual distances for all points in various orientations
                        scan.getAllOrientations().firstNotNullOfOrNull { orientedScan ->
                            val translationVectorCounts = orientedScan.beacons
                                .flatMap { orientedBeacon ->
                                    knownScan.beacons.map { knownBeacon -> knownBeacon - orientedBeacon }
                                }
                                .groupingBy { it }
                                .eachCount()
                            val (bestTranslationVector, count) = translationVectorCounts
                                .maxByOrNull { (_, count) -> count }
                                ?: error("expecting at least one translation vector")

                            // expecting 12 beacons minimum to match
                            if (count < 12) null else Pair(index, orientedScan + bestTranslationVector)
                        }
                    }
                }
            }

            // recursively examine
            return orientStep(
                remaining = remaining.withIndex().mapNotNull { (idx, scan) -> if (idx == index) null else scan },
                known = known + replacement,
            )
        }

        // start search from the beginning
        return orientStep(this.drop(1), listOf(this.first()))
    }

    fun part1(input: List<String>): Int {
        val orientedScans = parseInput(input).orientAll()
        return orientedScans.flatMap { it.beacons }.toSet().size
    }

    fun part2(input: List<String>): Int {
        val orientedScans = parseInput(input).orientAll()
        return orientedScans
            .flatMap { s1 -> orientedScans.map { s2 -> (s1.absolutePosition - s2.absolutePosition).norm() } }
            .maxOrNull()
            ?: error("no scans to orient")
    }

    // test
    val testInput = readInput("Day19_test")
    check(part1(testInput) == 79)
    check(part2(testInput) == 3621)

    val input = readInput("Day19")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
