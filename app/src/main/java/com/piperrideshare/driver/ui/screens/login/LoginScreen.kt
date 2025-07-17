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

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }

    var rememberMe by remember { mutableStateOf(userPrefs.rememberMe) }
    var email by remember { mutableStateOf(userPrefs.email ?: "") }
    var password by remember { mutableStateOf(userPrefs.password ?: "") }
    var deviceId by remember { mutableStateOf("device123") } // TODO: Firebase FCM / Device ID

    val loginResult by authViewModel.loginResult.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        if (rememberMe && email.isNotBlank() && password.isNotBlank()) {
            authViewModel.login(email, password, deviceId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            XmlDrawableImage(
                drawableRes = R.mipmap.ic_launcher,
                contentDescription = stringResource(R.string.app_name),
            )

            Spacer(modifier = Modifier.height(32.dp))

            PiperDriverOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(16.dp))

            PiperDriverOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                modifier = Modifier.fillMaxWidth(),
                isPassword = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            PiperDriverButton(
                text = "Login",
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                isAsync = true,
                onClickSuspend = {
                    authViewModel.login(email, password, deviceId)
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

            loginResult?.let { result ->
                if (result.isSuccess) {
                    LaunchedEffect(result) {
                        onLoginSuccess()
                    }
                    Text(
                        text = "Login successful! Welcome ${result.getOrNull()?.userId}",
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else if (result.isFailure) {
                    Text(
                        text = "Login failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

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
