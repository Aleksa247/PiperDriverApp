package com.piperrideshare.driver.ui.screens.login

import androidx.compose.foundation.background
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.piperrideshare.driver.R
import com.piperrideshare.driver.data.UserPreferences
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
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }

    // UI state management with hardcoded test credentials
    var rememberMe by remember { mutableStateOf(userPrefs.rememberMe) }
    var email by remember {
        mutableStateOf(
            if (userPrefs.email.isNullOrBlank()) "john.smith@thepiper.co" else userPrefs.email!!,
        )
    }
    var password by remember {
        mutableStateOf(
            if (userPrefs.password.isNullOrBlank()) "Test@123" else userPrefs.password!!,
        )
    }
    // @Thomas - Replace with actual Firebase FCM token or unique device identifier
    // Current implementation uses hardcoded value for testing purposes
    var deviceId by remember { mutableStateOf("device_002") }

    // Observe ViewModel state
    val loginResult by authViewModel.loginResult.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    // Auto-login if credentials are saved
    LaunchedEffect(key1 = Unit) {
        if (rememberMe && email.isNotBlank() && password.isNotBlank()) {
            authViewModel.login(email, password, deviceId)
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
            // App logo and branding
            XmlDrawableImage(
                drawableRes = R.mipmap.ic_launcher,
                contentDescription = stringResource(R.string.app_name),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email input field
            PiperDriverOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password input field with secure text entry
            PiperDriverOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                modifier = Modifier.fillMaxWidth(),
                isPassword = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Remember me checkbox
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

            // Login button with async operation support
            PiperDriverButton(
                text = "Login",
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                isAsync = true,
                onClickSuspend = {
                    // Perform login operation
                    authViewModel.login(email, password, deviceId)

                    // Save or clear credentials based on remember me setting
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

            // Display login result messages
            loginResult?.let { result ->
                if (result.isSuccess) {
                    // Navigate to home screen on successful login
                    LaunchedEffect(result) {
                        onLoginSuccess()
                    }
                    Text(
                        text = "Login successful! Welcome ${result.getOrNull()?.userId}",
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else if (result.isFailure) {
                    // Display error message for failed login
                    Text(
                        text = "Login failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        // Loading overlay displayed during authentication
        if (isLoading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Signing in...", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
