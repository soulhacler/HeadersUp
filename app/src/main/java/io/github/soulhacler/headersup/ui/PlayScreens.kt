package io.github.soulhacler.headersup.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.soulhacler.headersup.game.Flash
import io.github.soulhacler.headersup.game.GameState
import io.github.soulhacler.headersup.game.Tilt
import io.github.soulhacler.headersup.game.TiltDetector
import io.github.soulhacler.headersup.ui.theme.DeepGreen
import io.github.soulhacler.headersup.ui.theme.Muted
import io.github.soulhacler.headersup.ui.theme.NeonGreen
import io.github.soulhacler.headersup.ui.theme.PassAmber
import io.github.soulhacler.headersup.ui.theme.PitchDark
import io.github.soulhacler.headersup.ui.theme.Warning

@Composable
fun GetReadyScreen(countdown: Int, onQuit: () -> Unit) {
    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        QuitButton(onQuit, Modifier.align(Alignment.TopStart))
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("PLACE ON YOUR FOREHEAD", color = Muted, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            AnimatedContent(
                targetState = countdown,
                transitionSpec = {
                    (scaleIn(initialScale = 1.6f) + fadeIn()) togetherWith (scaleOut(targetScale = 0.6f) + fadeOut())
                },
                label = "countdown",
            ) { n ->
                Text("$n", color = NeonGreen, fontSize = 140.sp, fontWeight = FontWeight.Black)
            }
            Text("Tilt DOWN = correct  ·  Tilt UP = pass", color = Color.White, fontSize = 18.sp)
        }
    }
}

@Composable
fun PlayScreen(state: GameState, onTilt: (Tilt) -> Unit, onAnswer: (Boolean) -> Unit, onQuit: () -> Unit) {
    val context = LocalContext.current
    val currentOnTilt by rememberUpdatedState(onTilt)
    val detector = remember { TiltDetector(context) { currentOnTilt(it) } }
    DisposableEffect(detector) {
        detector.start()
        onDispose { detector.stop() }
    }

    val background by animateColorAsState(
        targetValue = when (state.flash) {
            Flash.CORRECT -> DeepGreen
            Flash.PASS -> Color(0xFF4A3205)
            Flash.NONE -> PitchDark
        },
        animationSpec = tween(200),
        label = "flash",
    )

    PitchBackground(color = background) {
        Box(Modifier.fillMaxSize().safeDrawingPadding().padding(12.dp)) {
            QuitButton(onQuit, Modifier.align(Alignment.TopStart))

            Text(
                "${state.secondsLeft}",
                modifier = Modifier.align(Alignment.TopCenter),
                color = if (state.secondsLeft <= 10) Warning else Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
            )

            Text(
                "✓ ${state.score}",
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                color = NeonGreen,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
            )

            AnimatedContent(
                targetState = state.flash to state.currentName,
                modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 48.dp),
                transitionSpec = {
                    (slideInVertically { it / 3 } + fadeIn(tween(180))) togetherWith
                        (slideOutVertically { -it / 3 } + fadeOut(tween(120)))
                },
                contentAlignment = Alignment.Center,
                label = "name",
            ) { (flash, name) ->
                when (flash) {
                    Flash.CORRECT -> BigText("CORRECT!", NeonGreen, 84.sp)
                    Flash.PASS -> BigText("PASS", PassAmber, 84.sp)
                    Flash.NONE -> BigText(name, Color.White, nameSize(name))
                }
            }

            if (!detector.isAvailable) {
                Text(
                    "No tilt sensor found. Use the buttons.",
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp),
                    color = Muted,
                    fontSize = 14.sp,
                )
            }

            Row(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OutlinedButton(
                    onClick = { onAnswer(false) },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, PassAmber),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PassAmber),
                ) { Text("PASS", fontWeight = FontWeight.Black, fontSize = 16.sp) }
                Button(
                    onClick = { onAnswer(true) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                ) { Text("CORRECT", fontWeight = FontWeight.Black, fontSize = 16.sp) }
            }
        }
    }
}

@Composable
private fun BigText(text: String, color: Color, size: TextUnit) {
    Text(
        text,
        modifier = Modifier.fillMaxWidth(),
        color = color,
        fontSize = size,
        lineHeight = size * 1.05f,
        fontWeight = FontWeight.Black,
        textAlign = TextAlign.Center,
        maxLines = 3,
    )
}

private fun nameSize(name: String): TextUnit = when {
    name.length <= 10 -> 80.sp
    name.length <= 16 -> 68.sp
    name.length <= 22 -> 56.sp
    else -> 46.sp
}

@Composable
private fun QuitButton(onQuit: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onQuit, modifier = modifier) {
        Text("✕  END", color = Muted, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ResultsScreen(state: GameState, onPlayAgain: () -> Unit, onMenu: () -> Unit) {
    val correct = state.guesses.filter { it.correct }.map { it.name }
    val passed = state.guesses.filterNot { it.correct }.map { it.name }

    Row(
        Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(
            Modifier.weight(0.8f).verticalScroll(rememberScrollState()).padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("FULL TIME", color = Muted, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Text("${state.score}", color = NeonGreen, fontSize = 96.sp, fontWeight = FontWeight.Black)
            Text(
                if (state.score == 1) "correct guess" else "correct guesses",
                color = Color.White,
                fontSize = 18.sp,
            )
            Text(
                "${state.category.title} · ${state.durationSeconds}s",
                color = Muted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onPlayAgain,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
            ) { Text("PLAY AGAIN", fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp) }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onMenu,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, NeonGreen),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
            ) { Text("MENU", fontWeight = FontWeight.Bold) }
        }

        NameList("CORRECT (${correct.size})", correct, NeonGreen, Modifier.weight(1f))
        NameList("PASSED (${passed.size})", passed, PassAmber, Modifier.weight(1f))
    }
}

@Composable
private fun NameList(title: String, names: List<String>, accent: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(title, color = accent, fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))
        if (names.isEmpty()) {
            Text("None", color = Muted, fontSize = 16.sp)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(names) { name ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.width(4.dp).height(20.dp).background(accent, RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(name, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
