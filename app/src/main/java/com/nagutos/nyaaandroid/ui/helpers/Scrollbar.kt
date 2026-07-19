package com.nagutos.nyaaandroid.ui.helpers

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A lightweight vertical scrollbar for a [LazyColumn].
 *
 * The thumb position is driven by the number of *pixels* scrolled (estimated from the
 * average height of the currently visible items), not by the first visible item index.
 * Tracking pixels — including the partial scroll offset inside the first visible item —
 * makes the thumb glide continuously instead of snapping item-by-item, which is what made
 * the old index-based version feel choppy. The bar fades in while scrolling and out at rest.
 */
fun Modifier.simpleVerticalScrollbar(
    state: LazyListState,
    width: Dp = 4.dp
): Modifier = composed {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration),
        label = "scrollbarAlpha"
    )
    // Captured in composable scope so it is available inside the (non-composable) DrawScope.
    val scrollbarColor = MaterialTheme.colorScheme.onSurfaceVariant

    drawWithContent {
        drawContent()

        val info = state.layoutInfo
        val visibleItems = info.visibleItemsInfo
        val totalItems = info.totalItemsCount

        // Nothing to draw while faded out or when everything already fits on screen.
        if (alpha <= 0f || visibleItems.isEmpty() || totalItems <= visibleItems.size) {
            return@drawWithContent
        }

        val viewportHeight = this.size.height
        val avgItemHeight = visibleItems.sumOf { it.size } / visibleItems.size.toFloat()
        val totalContentHeight = avgItemHeight * totalItems

        // Guard against the (rare) estimate where content isn't actually taller than the viewport.
        if (totalContentHeight <= viewportHeight) return@drawWithContent

        val first = visibleItems.first()
        // first.offset is the item's top relative to the viewport (negative once scrolled past,
        // positive when it sits below the top content padding), so subtracting it folds the
        // intra-item scroll into the pixel count for a smooth thumb.
        val scrolledPx = (first.index * avgItemHeight) - first.offset

        val thumbHeight = (viewportHeight / totalContentHeight) * viewportHeight
        val maxOffset = viewportHeight - thumbHeight
        val thumbOffsetY = (scrolledPx / (totalContentHeight - viewportHeight) * maxOffset)
            .coerceIn(0f, maxOffset)

        val barWidthPx = width.toPx()
        drawRoundRect(
            color = scrollbarColor.copy(alpha = alpha),
            topLeft = Offset(this.size.width - barWidthPx, thumbOffsetY),
            size = Size(barWidthPx, thumbHeight),
            cornerRadius = CornerRadius(barWidthPx / 2f)
        )
    }
}
