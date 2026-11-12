package com.example.wordpuzzle.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordpuzzle.data.GameRepository
import com.example.wordpuzzle.data.Level
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class WordResult { VALID, BONUS, ALREADY, INVALID }

data class GameUiState(
    val level: Level,
    val letters: List<Char>,
    val found: Set<String>,
    val bonusFound: Set<String>,
    val levelCount: Int,
    val paused: Boolean = false
) {
    val completed get() = found.containsAll(level.gridWords)
    val isLast get() = level.index == levelCount - 1
}


class GameViewModel(app: Application) : AndroidViewModel(app) {
    companion object { const val COINS_PER_LEVEL = 500 }

    private val repo = GameRepository(app)
    private val _coins = MutableStateFlow(repo.coins)
    val coins: StateFlow<Int> = _coins.asStateFlow()
    val levelCount = repo.levels.size

    private val _unlocked = MutableStateFlow(repo.unlocked)
    val unlocked: StateFlow<Int> = _unlocked.asStateFlow()

    private val _state = MutableStateFlow(build(repo.current.coerceIn(0, levelCount - 1), keep = true))
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private var advanceJob: Job? = null

    init { if (_state.value.completed) scheduleAdvance() }

    private fun build(i: Int, keep: Boolean): GameUiState {
        if (!keep) repo.clear(i)
        val lv = repo.levels[i]
        return GameUiState(lv, lv.letters.shuffled(), repo.found(i), repo.bonus(i), levelCount)
    }

    fun openLevel(i: Int) {
        if (i > _unlocked.value || i !in 0 until levelCount) return
        advanceJob?.cancel()
        val alreadyDone = repo.found(i).containsAll(repo.levels[i].gridWords)
        repo.current = i
        _state.value = build(i, keep = !alreadyDone)
    }

    fun restart() {
        advanceJob?.cancel()
        _state.value = build(_state.value.level.index, keep = false)
    }

    fun pause() = _state.update { if (it.completed) it else it.copy(paused = true) }
    fun resume() = _state.update { it.copy(paused = false) }

    fun shuffle() = _state.update { s ->
        var l = s.letters.shuffled()
        repeat(5) { if (l == s.letters && s.letters.distinct().size > 1) l = s.letters.shuffled() }
        s.copy(letters = l)
    }

    fun submit(word: String): WordResult {
        val s = _state.value
        if (s.completed || s.paused) return WordResult.INVALID
        return when {
            word in s.found || word in s.bonusFound -> WordResult.ALREADY
            word in s.level.gridWords -> {
                val n = s.copy(found = s.found + word)
                _state.value = n
                repo.save(n.level.index, n.found, n.bonusFound)
                if (n.completed) onCompleted(n)
                WordResult.VALID
            }
            word in s.level.bonus -> {
                val n = s.copy(bonusFound = s.bonusFound + word)
                _state.value = n
                repo.save(n.level.index, n.found, n.bonusFound)
                WordResult.BONUS
            }
            else -> WordResult.INVALID
        }
    }

    private fun onCompleted(s: GameUiState) {
        _coins.value += COINS_PER_LEVEL
        repo.coins = _coins.value
        val next = s.level.index + 1
        if (next < levelCount && next > _unlocked.value) {
            _unlocked.value = next
            repo.unlocked = next
        }
        scheduleAdvance()
    }

    private fun scheduleAdvance() {
        advanceJob?.cancel()
        advanceJob = viewModelScope.launch {
            delay(2200)
            val n = _state.value.level.index + 1
            if (n < levelCount) {
                repo.current = n
                _state.value = build(n, keep = false)
            }
        }
    }
}
