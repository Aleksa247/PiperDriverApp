package com.piperrideshare.driver.utils

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.drawVerticalScrollbar(scrollState: ScrollState): Modifier = drawWithContent {
    drawContent()

    val totalHeight = scrollState.maxValue + size.height
    val scrollRatio = size.height / totalHeight
    val scrollbarHeight = size.height * scrollRatio
    val scrollbarOffset = scrollState.value * scrollRatio

    drawRoundRect(
        color = Color.Gray.copy(alpha = 0.4f),
        topLeft = Offset(x = size.width - 6.dp.toPx(), y = scrollbarOffset),
        size = androidx.compose.ui.geometry.Size(4.dp.toPx(), scrollbarHeight),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
}
