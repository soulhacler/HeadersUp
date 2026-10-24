package io.github.soulhacler.headersup.game

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.soulhacler.headersup.data.Category
import io.github.soulhacler.headersup.data.Player
import io.github.soulhacler.headersup.data.PlayerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Phase { MENU, GET_READY, PLAYING, RESULTS }

enum class Flash { NONE, CORRECT, PASS }

data class Guess(val name: String, val correct: Boolean)

data class GameState(
    val phase: Phase = Phase.MENU,
    val category: Category = Category.ALL,
    val durationSeconds: Int = 60,
    val countdown: Int = COUNTDOWN_SECONDS,
    val currentName: String = "",
    val secondsLeft: Int = 60,
    val flash: Flash = Flash.NONE,
    val guesses: List<Guess> = emptyList(),
) {
    val score: Int get() = guesses.count { it.correct }
}

const val COUNTDOWN_SECONDS = 3
val ROUND_LENGTHS = listOf(30, 60, 90)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val players: List<Player> = PlayerRepository.load(application)
    private val feedback = Feedback(application)

    val categoryCounts: Map<Category, Int> =
        Category.entries.associateWith { c -> players.count { c.includes(it) } }

    var state by mutableStateOf(GameState())
        private set

    private var deck: List<String> = emptyList()
    private var deckIndex = 0
    private var roundJob: Job? = null
    private var flashJob: Job? = null

    fun selectCategory(category: Category) {
        state = state.copy(category = category)
    }

    fun selectDuration(seconds: Int) {
        state = state.copy(durationSeconds = seconds)
    }

    /** Starts a new round with the current category and length. */
    fun startRound() {
        roundJob?.cancel()
        flashJob?.cancel()
        // Shuffle once per round and walk the deck, so no name repeats within a round.
        deck = players.filter { state.category.includes(it) }.map { it.name }.distinct().shuffled()
        deckIndex = 0
        if (deck.isEmpty()) return

        state = state.copy(
            phase = Phase.GET_READY,
            countdown = COUNTDOWN_SECONDS,
            currentName = deck[0],
            secondsLeft = state.durationSeconds,
            flash = Flash.NONE,
            guesses = emptyList(),
        )

        roundJob = viewModelScope.launch {
            for (n in COUNTDOWN_SECONDS downTo 1) {
                state = state.copy(countdown = n)
                feedback.tick()
                delay(1000)
            }
            state = state.copy(phase = Phase.PLAYING)
            feedback.go()

            val endAt = SystemClock.elapsedRealtime() + state.durationSeconds * 1000L
            while (true) {
                val remainingMs = endAt - SystemClock.elapsedRealtime()
                if (remainingMs <= 0) break
                val secondsLeft = ((remainingMs + 999) / 1000).toInt()
                if (secondsLeft != state.secondsLeft) {
                    state = state.copy(secondsLeft = secondsLeft)
                    if (secondsLeft <= 5) feedback.tick()
                }
                delay(100)
            }
            endRound()
        }
    }

    /** Records the current player as correct or passed, then shows the next one. */
    fun answer(correct: Boolean) {
        if (state.phase != Phase.PLAYING || state.flash != Flash.NONE) return
        state = state.copy(
            flash = if (correct) Flash.CORRECT else Flash.PASS,
            guesses = state.guesses + Guess(state.currentName, correct),
        )
        if (correct) feedback.correct() else feedback.pass()

        flashJob = viewModelScope.launch {
            delay(FLASH_MS)
            deckIndex++
            if (deckIndex >= deck.size) {
                roundJob?.cancel()
                endRound()
            } else {
                state = state.copy(currentName = deck[deckIndex], flash = Flash.NONE)
            }
        }
    }

    fun onTilt(tilt: Tilt) = answer(correct = tilt == Tilt.DOWN)

    fun backToMenu() {
        roundJob?.cancel()
        flashJob?.cancel()
        state = state.copy(phase = Phase.MENU, flash = Flash.NONE)
    }

    private fun endRound() {
        flashJob?.cancel()
        state = state.copy(phase = Phase.RESULTS, flash = Flash.NONE, secondsLeft = 0)
        feedback.whistle()
    }

    override fun onCleared() {
        feedback.release()
    }

    private companion object {
        const val FLASH_MS = 750L
    }
}
