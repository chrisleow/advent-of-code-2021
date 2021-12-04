fun main() {

    data class BingoRecord(val move: Int, val number: Int)

    data class Grid(
        val numbers: Map<Pair<Int, Int>, Int>,
        val marks: Set<Pair<Int, Int>>,
        val bingo: BingoRecord?,
    )

    data class State(
        val move: Int,
        val remainingNumbers: List<Int>,
        val grids: List<Grid>
    )

    fun State.print() {
        println("Remaining Numbers: ${this.remainingNumbers}")
        println()
        this.grids.forEach { grid ->
            (0 until 5).forEach { y ->
                (0 until 5).forEach { x ->
                    val number = grid.numbers[Pair(x, y)]
                    val mark = if (Pair(x, y) in grid.marks) "*" else " "
                    print("$number$mark".padStart(4))
                }
                println()
            }
            if (grid.bingo != null) {
                println(" !!! BINGO (Move: ${grid.bingo.move}, Number: ${grid.bingo.number}) !!!")
            }
            println()
        }
    }

    fun parseInput(input: List<String>): State {
        val numbers = input[0]
            .split(",")
            .filter { it.isNotBlank() }
            .map { it.toInt() }

        val groupedLines: Sequence<List<String>> = sequence {
            val lines = mutableListOf<String>()
            input.drop(1).forEach { line ->
                if (line.isBlank()) {
                    if (lines.isNotEmpty()) {
                        yield(lines)
                        lines.clear()
                    }
                } else {
                    lines.add(line)
                }
            }
            if (lines.isNotEmpty()) {
                yield(lines)
            }
        }

        val grids = groupedLines.map { lines ->
            val gridNumbers = lines
                .flatMapIndexed { y, line ->
                    line.split(" ").filter { it.isNotBlank() }.mapIndexed { x, num ->
                        Pair(x, y) to num.toInt()
                    }
                }
                .toMap()
            Grid(numbers = gridNumbers, marks = emptySet(), bingo = null)
        }

        return State(
            move = 0,
            remainingNumbers = numbers,
            grids = grids.toList(),
        )
    }

    val winningPositions = run {
        val positions = sequence {
            // horizontals
            (0 until 5).forEach { y ->
                yield((0 until 5).map { x -> Pair(x, y) }.toSet())
            }
            // verticals
            (0 until 5).forEach { x ->
                yield((0 until 5).map { y -> Pair(x, y) }.toSet())
            }
        }
        positions.toList()
    }

    fun State.playOneMove(): State? {
        val currentMove = this.move + 1
        val playingNumber = this.remainingNumbers.firstOrNull()
            ?: return null

        val newGrids = this.grids.map { grid ->
            val matchingPositions = grid.numbers
                .filter { (_, number) -> number == playingNumber }
                .map { (pos, _) -> pos }
            val newMarks = grid.marks + matchingPositions

            // assess for "bingo"
            grid.copy(
                marks = newMarks,
                bingo = when {
                    grid.bingo != null -> grid.bingo
                    winningPositions.any { newMarks.containsAll(it) } -> {
                        BingoRecord(currentMove, playingNumber)
                    }
                    else -> null
                }
            )
        }

        return State(
            move = currentMove,
            remainingNumbers = this.remainingNumbers.drop(1),
            grids = newGrids,
        )
    }

    fun Grid.getScore(): Int {
        if (this.bingo == null)
            error("No bingo, no score :(")

        val unmarkedSum = this.numbers
            .filter { (pos, _) -> pos !in this.marks }
            .map { (_, number) -> number }
            .sum()
        return unmarkedSum * this.bingo.number
    }

    fun part1(input: List<String>): Int {
        val initialState = parseInput(input)
        val states = generateSequence(initialState) { it.playOneMove() }
        val winningState = states.first { state -> state.grids.any { it.bingo != null } }
        val winningGrid = winningState.grids.first { it.bingo != null }
        return winningGrid.getScore()
    }

    fun part2(input: List<String>): Int {
        val initialState = parseInput(input)
        val states = generateSequence(initialState) { it.playOneMove() }
        val lastState = states.first { state -> state.grids.all { it.bingo != null } }
        val losingGrid = lastState.grids.maxByOrNull { it.bingo?.move ?: 0 }
            ?: error("Shouldn't have a null losing grid.")
        return losingGrid.getScore()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day04_test")
    check(part1(testInput) == 4512)
    check(part2(testInput) == 1924)

    val input = readInput("Day04")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
