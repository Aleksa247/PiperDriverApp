package com.piperrideshare.driver.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

// Validation states matching iOS

enum class ValidationState {
    NONE,
    REQUIRED,
    INVALID
}

// Equivalent to iOS ButtonPrimary_Loading
@Composable
fun ButtonPrimaryLoading(
    title: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = !isLoading,
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(text = title)
        }
    }
}

// Equivalent to iOS Input_TextField
@Composable
fun InputTextField(
    text: String,
    onTextChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    hint: String = "",
    validationState: ValidationState = ValidationState.NONE,
    onEditingStarted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { if (placeholder.isNotEmpty()) Text(text = placeholder) },
            modifier = Modifier.fillMaxWidth(),
            isError = validationState != ValidationState.NONE,
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
        if (hint.isNotEmpty()) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = if (validationState == ValidationState.REQUIRED) AppColors.Red else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

// Equivalent to iOS Input_Password
@Composable
fun InputPassword(
    text: String,
    onTextChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    hint: String = "",
    validationState: ValidationState = ValidationState.NONE,
    onEditingStarted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { if (placeholder.isNotEmpty()) Text(text = placeholder) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = validationState != ValidationState.NONE,
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            }
        )
        if (hint.isNotEmpty()) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = if (validationState == ValidationState.REQUIRED) AppColors.Red else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

// Equivalent to iOS ButtonPill
@Composable
fun ButtonPill(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp) // Pill shape
    ) {
        Text(text = title)
    }
}

// Ride Request Card
@Composable
fun RideRequestCard(
    pickupAddress: String,
    distance: Double,
    eta: Int,
    earnings: Double,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Incoming Ride Request",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Progress bar placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.LightGray)
            ) {
                // Implement progress animation here
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Pickup: $pickupAddress",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$${String.format("%.2f", earnings / 100.0)}",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$eta min • ",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = String.format("%.1f miles", distance),
                    style = MaterialTheme.typography.displayMedium
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reject")
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Green),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Accept")
                }
            }
        }
    }
} 