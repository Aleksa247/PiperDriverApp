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
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }

    // UI state management with hardcoded test credentials
    var rememberMe by remember { mutableStateOf(userPrefs.rememberMe) }
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

    // Auto-login logic if rememberMe is true and credentials are available
    LaunchedEffect(Unit) {
        // @Thomas - BREAKPOINT HERE:: Auto-login triggered if rememberMe and credentials present
        if (rememberMe && email.isNotBlank() && password.isNotBlank()) {
            authViewModel.login(email, password)
        }
    }

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

            // @Thomas - BREAKPOINT HERE:: Remember Me checkbox UI
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                PiperDriverCheckbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    label = "Remember Me",
                    enabled = !isLoading,
                )
            }

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

                    // @Thomas - BREAKPOINT HERE:: Save or clear credentials based on rememberMe
                    if (rememberMe) {
                        userPrefs.rememberMe = true
                        userPrefs.email = email
                        userPrefs.password = password
                    } else {
                        userPrefs.clear()
                    }
                },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // @Thomas - BREAKPOINT HERE:: Show login results messages (success, failure, network error)
            loginResult?.let { result ->
                when (result) {
                    is ApiResult.Success -> {
                        LaunchedEffect(result) {
                            // @Thomas - BREAKPOINT HERE:: Login success detected
                            // Route through onboarding if callback provided (new flow)
                            // OnboardingCoordinator will check canGoOnline and redirect if complete
                            if (onLoginSuccessWithOnboarding != null) {
                                // Pass email from form, phone will be fetched from backend
                                onLoginSuccessWithOnboarding(email, "")
                            } else {
                                onLoginSuccess()
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
