package io.github.soulhacler.headersup.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.soulhacler.headersup.data.Category
import io.github.soulhacler.headersup.game.ROUND_LENGTHS
import io.github.soulhacler.headersup.ui.theme.Muted
import io.github.soulhacler.headersup.ui.theme.NeonGreen
import io.github.soulhacler.headersup.ui.theme.PitchSurface
import io.github.soulhacler.headersup.ui.theme.PitchSurfaceHigh

@Composable
fun MenuScreen(
    category: Category,
    durationSeconds: Int,
    categoryCounts: Map<Category, Int>,
    onCategory: (Category) -> Unit,
    onDuration: (Int) -> Unit,
    onStart: () -> Unit,
) {
    Row(
        Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(
            Modifier.weight(0.85f).fillMaxHeight().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("⚽", fontSize = 40.sp)
            Text(
                "HEADERS UP!",
                color = NeonGreen,
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
            Text("The footballer guessing game", color = Muted, fontSize = 16.sp)
            Spacer(Modifier.height(20.dp))
            HowToLine("1", "Hold the phone on your forehead, screen facing out.")
            HowToLine("2", "Your mates describe the footballer on screen.")
            HowToLine("3", "Tilt DOWN when you get it right. Tilt UP to pass.")
        }

        Column(
            Modifier.weight(1.15f).fillMaxHeight().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionLabel("CATEGORY")
            Category.entries.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEach { c ->
                        CategoryCard(
                            category = c,
                            count = categoryCounts[c] ?: 0,
                            selected = c == category,
                            onClick = { onCategory(c) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            SectionLabel("ROUND LENGTH")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ROUND_LENGTHS.forEach { seconds ->
                    DurationChip(
                        seconds = seconds,
                        selected = seconds == durationSeconds,
                        onClick = { onDuration(seconds) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
            ) {
                Text("KICK OFF", fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
private fun HowToLine(number: String, text: String) {
    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Text(number, color = NeonGreen, fontWeight = FontWeight.Black, fontSize = 16.sp, modifier = Modifier.padding(end = 10.dp))
        Text(text, color = Color.White, fontSize = 15.sp)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
}

@Composable
private fun CategoryCard(
    category: Category,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container by animateColorAsState(if (selected) PitchSurfaceHigh else PitchSurface, label = "card")
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = container,
        border = BorderStroke(2.dp, if (selected) NeonGreen else MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
                category.title,
                color = if (selected) NeonGreen else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${category.subtitle} · $count",
                color = Muted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DurationChip(seconds: Int, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val container by animateColorAsState(if (selected) NeonGreen else PitchSurface, label = "chip")
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = container,
        border = BorderStroke(2.dp, if (selected) NeonGreen else MaterialTheme.colorScheme.outline),
    ) {
        Text(
            "${seconds}s",
            modifier = Modifier.fillMaxWidth().padding(PaddingValues(vertical = 12.dp)),
            color = if (selected) Color.Black else Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
