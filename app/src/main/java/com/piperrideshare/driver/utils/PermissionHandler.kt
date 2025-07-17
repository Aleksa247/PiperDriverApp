package com.piperrideshare.driver.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
    permissions: List<String>,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: (() -> Unit)? = null,
) {
    val multiplePermissionsState = rememberMultiplePermissionsState(permissions)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(multiplePermissionsState.allPermissionsGranted) {
        if (multiplePermissionsState.allPermissionsGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied?.invoke()
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            multiplePermissionsState.launchMultiplePermissionRequest()
        }
    }
}
