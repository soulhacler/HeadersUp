package io.github.soulhacler.headersup.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import io.github.soulhacler.headersup.ui.theme.NeonGreen
import io.github.soulhacler.headersup.ui.theme.PitchDark

/** Dark background with faint pitch markings drawn behind the content. */
@Composable
fun PitchBackground(
    modifier: Modifier = Modifier,
    color: Color = PitchDark,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.fillMaxSize().background(color)) {
        Canvas(Modifier.fillMaxSize()) {
            val line = NeonGreen.copy(alpha = 0.07f)
            val stroke = Stroke(width = 3f)
            val inset = 24f
            drawRect(line, Offset(inset, inset), Size(size.width - 2 * inset, size.height - 2 * inset), style = stroke)
            drawLine(line, Offset(size.width / 2, inset), Offset(size.width / 2, size.height - inset), strokeWidth = 3f)
            drawCircle(line, radius = size.minDimension * 0.18f, center = center, style = stroke)
            drawCircle(line, radius = 6f, center = center)
            val boxH = size.height * 0.5f
            val boxW = size.width * 0.12f
            drawRect(line, Offset(inset, center.y - boxH / 2), Size(boxW, boxH), style = stroke)
            drawRect(line, Offset(size.width - inset - boxW, center.y - boxH / 2), Size(boxW, boxH), style = stroke)
        }
        content()
    }
}
