package com.piperrideshare.driver.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun PiperDriverAlert(
    showLoading: Boolean,
    statusText: String
) {
    if (!showLoading) return

    AlertDialog(
        onDismissRequest = {}, // Prevent dismiss
        confirmButton = {},
        title = {
            Text(text = "Please wait")
        },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(text = statusText)
            }
        }
    )
}
