package io.github.soulhacler.headersup

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.soulhacler.headersup.game.GameViewModel
import io.github.soulhacler.headersup.game.Phase
import io.github.soulhacler.headersup.ui.GetReadyScreen
import io.github.soulhacler.headersup.ui.MenuScreen
import io.github.soulhacler.headersup.ui.PitchBackground
import io.github.soulhacler.headersup.ui.PlayScreen
import io.github.soulhacler.headersup.ui.ResultsScreen
import io.github.soulhacler.headersup.ui.theme.HeadersUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            HeadersUpTheme {
                HeadersUpApp()
            }
        }
    }
}

@Composable
private fun HeadersUpApp(vm: GameViewModel = viewModel()) {
    val state = vm.state

    BackHandler(enabled = state.phase != Phase.MENU) { vm.backToMenu() }

    // Stop the screen flipping between the two landscape rotations mid-round.
    val activity = LocalContext.current as? Activity
    val lockRotation = state.phase == Phase.GET_READY || state.phase == Phase.PLAYING
    LaunchedEffect(lockRotation) {
        activity?.requestedOrientation =
            if (lockRotation) ActivityInfo.SCREEN_ORIENTATION_LOCKED else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    PitchBackground {
        Crossfade(targetState = state.phase, animationSpec = tween(300), label = "phase") { phase ->
            when (phase) {
                Phase.MENU -> MenuScreen(
                    category = state.category,
                    durationSeconds = state.durationSeconds,
                    categoryCounts = vm.categoryCounts,
                    onCategory = vm::selectCategory,
                    onDuration = vm::selectDuration,
                    onStart = vm::startRound,
                )
                Phase.GET_READY -> GetReadyScreen(countdown = state.countdown, onQuit = vm::backToMenu)
                Phase.PLAYING -> PlayScreen(
                    state = state,
                    onTilt = vm::onTilt,
                    onAnswer = vm::answer,
                    onQuit = vm::backToMenu,
                )
                Phase.RESULTS -> ResultsScreen(state = state, onPlayAgain = vm::startRound, onMenu = vm::backToMenu)
            }
        }
    }
}
