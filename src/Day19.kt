import kotlin.math.abs

fun main() {

    data class Vector(val x: Int, val y: Int, val z: Int) {
        operator fun plus(other: Vector) = Vector(x + other.x, y + other.y, z + other.z)
        operator fun minus(other: Vector) = Vector(x - other.x, y - other.y, z - other.z)
        val norm = abs(x) + abs(y) + abs(z)
    }

    data class OrientedScan(
        val id: Int,
        val beacons: Set<Vector>,
        val mutualDistances: Set<Int>,
        val position: Vector,
    )

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
            .mapIndexed { index, lines ->
                val beacons = lines
                    .mapNotNull { regex.matchEntire(it)?.groupValues }
                    .map { gv -> Vector(gv[1].toInt(), gv[2].toInt(), gv[3].toInt()) }

                // some fields here will be useful later, specifically position and translations norms, where
                // position will be modified as we translate points, and mutual distances allows easy filtering
                // between scanners
                OrientedScan(
                    id = index,
                    beacons = beacons.toSet(),
                    position = Vector(0, 0, 0),
                    mutualDistances = beacons
                        .flatMap { b1 -> beacons.map { b2 -> (b1 - b2).norm } }
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

    fun OrientedScan.transform(transform: (Vector) -> Vector) = copy(
        beacons = beacons.map { transform(it) }.toSet(),
        position = transform(position),
    )

    operator fun OrientedScan.plus(other: Vector) = this.transform { it + other }

    fun OrientedScan.allOrientations() = orientationTransformers
        .asSequence()
        .map { transformer -> this.transform(transformer) }

    fun List<OrientedScan>.orientAll(): List<OrientedScan> {
        tailrec fun orientStep(scans: List<OrientedScan>, knownScans: List<OrientedScan>): List<OrientedScan> {
            if (scans.isEmpty()) return knownScans

            // first-pass filter, we know that if at least 66 == 12 * (12 - 1) / 2 of the norms don't match in
            // length, we shouldn't investigate further...  much more efficent than examining all orientations
            // up-front!
            val matchingScanPairsFirstPass = knownScans
                .asSequence()
                .flatMap { knownScan -> scans.map { Pair(knownScan, it) } }
                .filter { (knownScan, scan) -> (knownScan.mutualDistances intersect scan.mutualDistances).size >= 66 }

            // now have to work harder, examine mutual distances for all points in all orientations
            // we do this by counting translation vectors between existing and known scans, expecting the minimum
            // number (12) which proves that there are 12 beacons in common
            val newOrientedScan = matchingScanPairsFirstPass
                .flatMap { (knownScan, scan) -> scan.allOrientations().map { Pair(knownScan,it) } }
                .firstNotNullOf { (knownScan, orientedScan) ->
                    val translationVectorCounts = knownScan.beacons
                        .flatMap { knownBeacon -> orientedScan.beacons.map { beacon -> knownBeacon - beacon } }
                        .groupingBy { it }
                        .eachCount()
                    val (mostCommonTranslationVector, maxCount) = translationVectorCounts
                        .maxByOrNull { (_, count) -> count }
                        ?: error("expecting at least one translation vector")

                    // expecting 12 beacons minimum to match, ensure we translate as well as orient result
                    if (maxCount < 12) {
                        null
                    } else {
                        orientedScan + mostCommonTranslationVector
                    }
                }

            // move scan to "known", so we may try again
            return orientStep(scans.filter { it.id != newOrientedScan.id }, knownScans + newOrientedScan)
        }

        // start search assuming scanner 0 is the correct orientation and at absolute 0 position
        return orientStep(this.drop(1), listOf(this.first()))
    }

    fun part1(input: List<String>): Int {
        val orientedScans = parseInput(input).orientAll()
        return orientedScans.flatMap { it.beacons }.toSet().size
    }

    fun part2(input: List<String>): Int {
        val orientedScans = parseInput(input).orientAll()
        return orientedScans
            .flatMap { s1 -> orientedScans.map { s2 -> (s1.position - s2.position).norm } }
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
