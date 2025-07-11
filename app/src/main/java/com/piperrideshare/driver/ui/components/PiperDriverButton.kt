package com.piperrideshare.driver.ui.components

import androidx.compose.material3.Button
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
    ) {
        Text(text)
    }
}
