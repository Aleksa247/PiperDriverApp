package com.piperrideshare.driver.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun PiperDriverButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isAsync: Boolean = false,
    onClick: () -> Unit = {},
    onClickSuspend: (suspend () -> Unit)? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
) {
    val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = {
            if (isAsync && onClickSuspend != null) {
                coroutineScope.launch {
                    onClickSuspend()
                }
            } else {
                onClick()
            }
        },
        modifier = modifier,
        enabled = enabled,
        colors = colors,
    ) {
        Text(text)
    }
}
