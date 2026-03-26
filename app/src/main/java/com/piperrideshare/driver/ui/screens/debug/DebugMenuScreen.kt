package com.piperrideshare.driver.ui.screens.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piperrideshare.driver.BuildConfig
import com.piperrideshare.driver.data.local.DebugSettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugMenuViewModel @Inject constructor(
    private val debugSettingsManager: DebugSettingsManager
) : ViewModel() {

    val customUrl = debugSettingsManager.customBaseUrlFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val defaultUrl: String = BuildConfig.BASE_URL

    fun saveCustomUrl(url: String) {
        viewModelScope.launch {
            debugSettingsManager.setCustomBaseUrl(url)
        }
    }

    fun clearCustomUrl() {
        viewModelScope.launch {
            debugSettingsManager.clearCustomBaseUrl()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugMenuScreen(
    onBack: () -> Unit,
    viewModel: DebugMenuViewModel = hiltViewModel()
) {
    val customUrl by viewModel.customUrl.collectAsState()
    var urlInput by remember(customUrl) { mutableStateOf(customUrl ?: "") }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "API Environment",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Default: ${viewModel.defaultUrl}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (customUrl != null) {
                Text(
                    text = "Current Override: $customUrl",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("Custom API URL (e.g., https://xxxx.ngrok.io)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                trailingIcon = {
                    if (urlInput.isNotEmpty()) {
                        IconButton(onClick = { urlInput = "" }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Clear")
                        }
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.saveCustomUrl(urlInput)
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = urlInput.isNotBlank()
                ) {
                    Text("Save & Restart App")
                }

                OutlinedButton(
                    onClick = {
                        viewModel.clearCustomUrl()
                        urlInput = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset to Default")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ Note",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "After changing the URL, you must restart the app for changes to take effect (clear from recents and relaunch).",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
