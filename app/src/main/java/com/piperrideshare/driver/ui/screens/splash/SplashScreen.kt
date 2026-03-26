package com.piperrideshare.driver.ui.screens.splash

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.piperrideshare.driver.R
import com.piperrideshare.driver.ui.components.XmlDrawableImage
import com.piperrideshare.driver.utils.PermissionHandler
import kotlinx.coroutines.delay
import timber.log.Timber

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    var permissionsRequested by remember { mutableStateOf(false) }
    var permissionsGranted by remember { mutableStateOf(false) }
    var showPermissionDeniedMessage by remember { mutableStateOf(false) }

    // Define required permissions
    val permissions = remember {
        mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Effect to check session once permissions are granted
    LaunchedEffect(key1 = permissionsGranted) {
        if (permissionsGranted) {
            Timber.d("✅ PERMISSIONS: All permissions granted, checking session")
            viewModel.checkSession()
        }
    }

    // Effect to handle navigation based on view model state
    LaunchedEffect(key1 = uiState) {
        if (permissionsGranted) {
             when(uiState) {
                is SplashState.NavigateToHome -> {
                     Timber.d("🚀 SESSION: Valid session found, navigating to HOME")
                     delay(500)
                     onNavigateToHome()
                }
                is SplashState.NavigateToLogin -> {
                     Timber.d("🔒 SESSION: No valid session, navigating to LOGIN")
                     delay(500)
                     onNavigateToLogin()
                }
                else -> { /* Loading */ }
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        // Start permission request after a short delay to show splash screen
        delay(1500)
        permissionsRequested = true
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            XmlDrawableImage(
                drawableRes = R.mipmap.ic_launcher,
                contentDescription = stringResource(R.string.app_name),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(32.dp))

            when {
                !permissionsRequested -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading...",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                showPermissionDeniedMessage -> {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⚠️ Permissions Required",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This app requires location and notification permissions to function properly. You can grant them later in settings.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    Timber.d("🔄 PERMISSIONS: User chose to continue without permissions")
                                    // Manually trigger session check since permissionsGranted won't flip to true
                                    viewModel.checkSession()
                                    permissionsGranted = true 
                                }
                            ) {
                                Text("Continue Anyway")
                            }
                        }
                    }
                }

                permissionsRequested && !permissionsGranted && !showPermissionDeniedMessage -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Requesting permissions...",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                
                permissionsGranted && uiState is SplashState.Loading -> {
                     CircularProgressIndicator()
                     Spacer(modifier = Modifier.height(16.dp))
                     Text(
                        text = "Checking session...",
                        style = MaterialTheme.typography.bodyMedium,
                     )
                }

                permissionsGranted -> {
                    Text(
                        text = "✅ Ready!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Handle permission requests
    if (permissionsRequested && !permissionsGranted && !showPermissionDeniedMessage) {
        PermissionHandler(
            permissions = permissions,
            onPermissionGranted = {
                Timber.d("✅ PERMISSIONS: All permissions granted in splash screen")
                Toast.makeText(
                    context,
                    "Permissions granted! You're ready to drive.",
                    Toast.LENGTH_SHORT
                ).show()
                permissionsGranted = true
            },
            onPermissionDenied = {
                Timber.w("⚠️ PERMISSIONS: Some permissions denied in splash screen")
                Toast.makeText(
                    context,
                    "Some permissions were denied. You can grant them later in settings.",
                    Toast.LENGTH_LONG
                ).show()
                showPermissionDeniedMessage = true
            }
        )
    }

}
