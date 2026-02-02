package com.piperrideshare.driver.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.piperrideshare.driver.BuildConfig
import com.piperrideshare.driver.R
import com.piperrideshare.driver.data.UserPreferences
import com.piperrideshare.driver.data.network.ApiResult
import com.piperrideshare.driver.ui.components.PiperDriverButton
import com.piperrideshare.driver.ui.components.PiperDriverCheckbox
import com.piperrideshare.driver.ui.components.PiperDriverOutlinedTextField
import com.piperrideshare.driver.ui.components.XmlDrawableImage
import com.piperrideshare.driver.ui.viewModel.WebSocketViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.first

/**
 * LoginScreen - Authentication interface for the driver app
 *
 * This composable provides the login interface where drivers can authenticate
 * to access the ride-sharing platform. It includes:
 * - Email and password input fields
 * - Remember me functionality
 * - Loading states and error handling
 * - Auto-login for saved credentials
 *
 * The screen integrates with AuthViewModel for business logic and UserPreferences
 * for persistent storage of login credentials.
 *
 * @param onLoginSuccess Callback function triggered on successful authentication
 * @param authViewModel ViewModel for managing authentication state (injected via Hilt)
 *
 * @author Thomas Woodfin
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onLoginSuccessWithOnboarding: ((email: String, phone: String) -> Unit)? = null,
    onNavigateToDebugMenu: (() -> Unit)? = null,
    onNavigateToRegister: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    webSocketViewModel: WebSocketViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }

    // UI state management with hardcoded test credentials

    var email by remember {
        mutableStateOf(
            when {
                BuildConfig.DEBUG -> userPrefs.email.takeIf { !it.isNullOrBlank() } ?: BuildConfig.DEFAULT_EMAIL
                else -> userPrefs.email.orEmpty()
            },
        )
    }

    var password by remember {
        mutableStateOf(
            when {
                BuildConfig.DEBUG -> userPrefs.password.takeIf { !it.isNullOrBlank() } ?: BuildConfig.DEFAULT_PASSWORD
                else -> userPrefs.password.orEmpty()
            },
        )
    }

    // Observe ViewModel state
    val loginResult by authViewModel.loginResult.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    
    // Observe driver state from WebSocket
    val driverState by webSocketViewModel.driverState.collectAsState(initial = null)

    // Reset login state when entering login screen to prevent stale success from auto-navigating
    LaunchedEffect(Unit) {
        authViewModel.resetLoginState()
    }
    
    // Auto-login disabled: Users must explicitly click Login or Register
    // This gives new users a chance to navigate to registration
    // LaunchedEffect(Unit) {
    //     if (email.isNotBlank() && password.isNotBlank()) {
    //         authViewModel.login(email, password)
    //     }
    // }

    // Main login interface
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // @Thomas - BREAKPOINT HERE:: App logo shown here
            // Long-press on logo opens Debug Menu in DEBUG builds
            Box(
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            onNavigateToDebugMenu?.invoke()
                        }
                    )
                }
            ) {
                XmlDrawableImage(
                    drawableRes = R.mipmap.ic_launcher,
                    contentDescription = stringResource(R.string.app_name),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // @Thomas - BREAKPOINT HERE:: Email input field UI
            PiperDriverOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // @Thomas - BREAKPOINT HERE:: Password input field UI
            PiperDriverOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                modifier = Modifier.fillMaxWidth(),
                isPassword = true,
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(16.dp))



            Spacer(modifier = Modifier.height(32.dp))

            // @Thomas - BREAKPOINT HERE:: Login button UI and click handler
            PiperDriverButton(
                text = "Login",
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                isAsync = true,
                onClickSuspend = {
                    // @Thomas - BREAKPOINT HERE:: Login button clicked; call ViewModel.login
                    authViewModel.login(email, password)

                    // Always save credentials for auto-login
                    userPrefs.rememberMe = true
                    userPrefs.email = email
                    userPrefs.password = password
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Register option
            androidx.compose.material3.TextButton(onClick = onNavigateToRegister) {
                Text("Don't have an account? Register")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // @Thomas - BREAKPOINT HERE:: Show login results messages (success, failure, network error)
            loginResult?.let { result ->
                when (result) {
                    is ApiResult.Success -> {
                        LaunchedEffect(result) {
                            // @Thomas - BREAKPOINT HERE:: Login success detected
                            // Initialize WebSocket to receive driver model
                            webSocketViewModel.initialize()
                            
                            // Wait for driver state to be populated from WebSocket
                            // The server sends driver_model_changed immediately upon connection
                            val timeoutMs = 5000L
                            val driver = withTimeoutOrNull(timeoutMs) {
                                var attempts = 0
                                while (attempts < 50) {
                                    val currentState = webSocketViewModel.driverState.first()
                                    if (currentState != null) {
                                        return@withTimeoutOrNull currentState
                                    }
                                    delay(100)
                                    attempts++
                                }
                                null
                            }
                            
                            if (driver == null) {
                                // Timeout - driver state not received
                                // Default to onboarding flow for safety
                                if (onLoginSuccessWithOnboarding != null) {
                                    onLoginSuccessWithOnboarding(email, "")
                                } else {
                                    onLoginSuccess()
                                }
                                return@LaunchedEffect
                            }
                            
                            // Check driver's active status
                            if (driver.isActive) {
                                // Driver is active - go directly to home
                                onLoginSuccess()
                            } else {
                                // Driver is not active - route to onboarding
                                if (onLoginSuccessWithOnboarding != null) {
                                    onLoginSuccessWithOnboarding(email, driver.phone)
                                } else {
                                    onLoginSuccess()
                                }
                            }
                        }
                        Text(
                            text = "Login successful! Welcome ${result.data.id}",
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    is ApiResult.Failure -> {
                        Text(
                            text = "Login failed: ${result.message}",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    is ApiResult.NetworkError -> {
                        Text(
                            text = "Network error: Please check your internet connection.",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }

        // Loading overlay during login process
        if (isLoading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // @Thomas - BREAKPOINT HERE:: Show CircularProgressIndicator during login
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Signing in...", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
