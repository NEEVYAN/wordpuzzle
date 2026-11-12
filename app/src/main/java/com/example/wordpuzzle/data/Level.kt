package com.example.wordpuzzle.data

data class LevelData(val letters: String, val words: List<String>, val bonus: List<String>)

data class PlacedWord(val word: String, val row: Int, val col: Int, val horizontal: Boolean) {
    fun cells(): List<Pair<Int, Int>> =
        word.indices.map { k -> (row + if (horizontal) 0 else k) to (col + if (horizontal) k else 0) }
}

class Level(
    val index: Int,
    val letters: List<Char>,
    val rows: Int,
    val cols: Int,
    val placed: List<PlacedWord>,
    val bonus: Set<String>
) {
    val gridWords: Set<String> = placed.map { it.word }.toSet()
    val cells: Map<Pair<Int, Int>, Char> = buildMap {
        placed.forEach { p -> p.cells().forEachIndexed { i, c -> put(c, p.word[i]) } }
    }
}

/** Builds a crossword layout from a plain word list. Words that cannot be crossed become bonus words. */
object CrosswordGenerator {
    fun build(index: Int, d: LevelData): Level {
        val words = d.words.map { it.uppercase() }.distinct().sortedByDescending { it.length }
        val cells = HashMap<Pair<Int, Int>, Char>()
        val placed = ArrayList<PlacedWord>()
        val leftovers = ArrayList<String>()

        fun add(p: PlacedWord) {
            p.cells().forEachIndexed { i, c -> cells[c] = p.word[i] }
            placed += p
        }

        fun fits(p: PlacedWord): Boolean {
            val dr = if (p.horizontal) 0 else 1
            val dc = 1 - dr
            if (cells.containsKey(p.row - dr to p.col - dc)) return false
            if (cells.containsKey(p.row + dr * p.word.length to p.col + dc * p.word.length)) return false
            var cross = 0
            p.cells().forEachIndexed { i, (r, c) ->
                val ex = cells[r to c]
                if (ex != null) {
                    if (ex != p.word[i]) return false
                    cross++
                } else if (cells.containsKey(r + dc to c + dr) || cells.containsKey(r - dc to c - dr)) {
                    return false
                }
            }
            return cross in 1 until p.word.length
        }

        add(PlacedWord(words[0], 0, 0, true))
        for (w in words.drop(1)) {
            var done = false
            loop@ for (p in placed.toList()) for (i in w.indices) for (j in p.word.indices) {
                if (w[i] != p.word[j]) continue
                val cand = if (p.horizontal) PlacedWord(w, p.row - i, p.col + j, false)
                else PlacedWord(w, p.row + j, p.col - i, true)
                if (fits(cand)) { add(cand); done = true; break@loop }
            }
            if (!done) leftovers += w
        }

        val minR = placed.minOf { it.row }
        val minC = placed.minOf { it.col }
        val shifted = placed.map { it.copy(row = it.row - minR, col = it.col - minC) }
        val allCells = shifted.flatMap { it.cells() }
        return Level(
            index, d.letters.uppercase().toList(),
            allCells.maxOf { it.first } + 1, allCells.maxOf { it.second } + 1,
            shifted, (d.bonus.map { it.uppercase() } + leftovers).toSet()
        )
    }
}
