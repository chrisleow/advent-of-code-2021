sealed class Packet {
    abstract val version: Int
    data class Literal(override val version: Int, val value: Long) : Packet()
    data class Operator(override val version: Int, val typeId: Int, val packets: List<Packet>) : Packet()
}

fun main() {

    fun String.toBinary(): String = this
        .replace("0", "0000")
        .replace("1", "0001")
        .replace("2", "0010")
        .replace("3", "0011")
        .replace("4", "0100")
        .replace("5", "0101")
        .replace("6", "0110")
        .replace("7", "0111")
        .replace("8", "1000")
        .replace("9", "1001")
        .replace("A", "1010")
        .replace("B", "1011")
        .replace("C", "1100")
        .replace("D", "1101")
        .replace("E", "1110")
        .replace("F", "1111")

    fun parseHexPacket(hexString: String): Packet {
        val binaryString = hexString.toBinary()

        fun parse(index: Int): Pair<Int, Packet> {
            val version = binaryString.slice(index until index + 3).toInt(2)

            fun parseLiteral(subIndex: Int): Pair<Int, Packet> {
                tailrec fun parseNumbers(index: Int, binaryNumber: String): Pair<Int, String> {
                    val slice = binaryString.slice(index + 1 until index + 5)
                    return when (binaryString[index]) {
                        '0' -> Pair(index + 5, binaryNumber + slice)
                        '1' -> parseNumbers(index + 5, binaryNumber + slice)
                        else -> error("Not a binary value")
                    }
                }
                val (nextIndex, binaryNumber) = parseNumbers(subIndex, "")
                return Pair(nextIndex, Packet.Literal(version, binaryNumber.toLong(2)))
            }

            fun parseOperator(subIndex: Int, typeId: Int): Pair<Int, Packet> {
                return when (val lengthTypeId = binaryString[subIndex]) {
                    '0' -> {
                        val length = binaryString.slice(subIndex + 1 until subIndex + 16).toInt(2)
                        val stopIndex = subIndex + 16 + length
                        tailrec fun parseSubPackets(innerIndex: Int, packets: List<Packet>): List<Packet> {
                            return if (innerIndex >= stopIndex) {
                                packets
                            } else {
                                val (nextSubIndex, packet) = parse(innerIndex)
                                parseSubPackets(nextSubIndex, packets + packet)
                            }
                        }
                        Pair(stopIndex, Packet.Operator(version, typeId, parseSubPackets(subIndex + 16, emptyList())))
                    }
                    '1' -> {
                        val count = binaryString.slice(subIndex + 1 until subIndex + 12).toInt(2)
                        val initialState = Pair(subIndex + 12, emptyList<Packet>())
                        val (endIndex, packets) = (0 until count).fold(initialState) { (innerIndex, packets), _ ->
                            val (nextInnerIndex, packet) = parse(innerIndex)
                            nextInnerIndex to (packets + packet)
                        }
                        Pair(endIndex, Packet.Operator(version, typeId, packets))
                    }
                    else -> error("Unknown length type ID '$lengthTypeId'.")
                }
            }

            // main parsing logic
            return when (val typeId = binaryString.slice(index + 3 until index + 6).toInt(2)) {
                4 -> parseLiteral(index + 6)
                else -> parseOperator(index + 6, typeId)
            }
        }

        return parse(0).second
    }

    fun Packet.evaluate(): Long {
        return when (this) {
            is Packet.Literal -> this.value
            is Packet.Operator -> when (this.typeId) {
                0 -> this.packets.sumOf { it.evaluate() }
                1 -> this.packets.fold(1L) { acc, p -> acc * p.evaluate() }
                2 -> this.packets.minOf { it.evaluate() }
                3 -> this.packets.maxOf { it.evaluate() }
                5 -> if (this.packets[0].evaluate() > this.packets[1].evaluate()) 1 else 0
                6 -> if (this.packets[0].evaluate() < this.packets[1].evaluate()) 1 else 0
                7 -> if (this.packets[0].evaluate() == this.packets[1].evaluate()) 1 else 0
                else -> error("Unknown type ID '$typeId'.")
            }
        }
    }

    fun part1(input: List<String>): Int {
        fun flatten(packet: Packet): Sequence<Packet> = sequence {
            yield(packet)
            if (packet is Packet.Operator) {
                yieldAll(packet.packets.flatMap { flatten(it) })
            }
        }

        val hexString = input.joinToString("").trim()
        return flatten(parseHexPacket(hexString)).sumOf { it.version }
    }

    fun part2(input: List<String>): Long {
        val hexString = input.joinToString("").trim()
        return parseHexPacket(hexString).evaluate()
    }

    val input = readInput("Day16")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
