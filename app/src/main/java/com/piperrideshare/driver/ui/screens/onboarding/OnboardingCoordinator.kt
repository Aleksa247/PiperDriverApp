package com.piperrideshare.driver.ui.screens.onboarding

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Main OnboardingCoordinator - Routes to appropriate onboarding step.
 * Matches iOS OnboardingCoordinator.swift structure.
 */
@Composable
fun OnboardingCoordinator(
    email: String,
    phone: String,
    viewModel: OnboardingViewModel = hiltViewModel(),
    onComplete: () -> Unit,
    onLogout: () -> Unit,
) {
    val onboardingStatus by viewModel.onboardingStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    // Fetch status on first load
    LaunchedEffect(Unit) {
        viewModel.fetchOnboardingStatus()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading && onboardingStatus == null -> {
                // Initial loading
                LoadingView()
            }
            errorMessage != null && onboardingStatus == null -> {
                // Error loading status
                ErrorView(
                    message = errorMessage ?: "Unknown error",
                    onRetry = { viewModel.fetchOnboardingStatus() },
                )
            }
            onboardingStatus != null -> {
                val status = onboardingStatus!!
                
                if (status.canGoOnline) {
                    // All complete - notify parent
                    LaunchedEffect(Unit) {
                        onComplete()
                    }
                } else {
                    // Route based on current step
                    val currentStep = status.getCurrentStep()
                    
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top bar with logout and refresh
                        OnboardingTopBar(
                            onLogout = onLogout,
                            onRefresh = { viewModel.fetchOnboardingStatus() },
                        )
                        
                        when (currentStep) {
                            "VERIFY_EMAIL" -> {
                                EmailVerificationScreen(
                                    email = email,
                                    viewModel = viewModel,
                                    onVerified = { viewModel.fetchOnboardingStatus() },
                                )
                            }
                            "VERIFY_PHONE" -> {
                                PhoneVerificationScreen(
                                    phone = phone,
                                    viewModel = viewModel,
                                    onVerified = { viewModel.fetchOnboardingStatus() },
                                )
                            }
                            "COMPLETE_CHECKR_INVITATION" -> {
                                BackgroundCheckScreen(
                                    viewModel = viewModel,
                                )
                            }
                            "WAIT_FOR_BACKGROUND_CHECK" -> {
                                WaitingForBackgroundCheckView()
                            }
                            "UNDER_REVIEW" -> {
                                UnderReviewView()
                            }
                            "VERIFICATION_REJECTED" -> {
                                VerificationRejectedView()
                            }
                            "SETUP_STRIPE" -> {
                                StripeSetupView(
                                    email = email,
                                    viewModel = viewModel,
                                    onOpenUrl = { url ->
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    },
                                )
                            }
                            "COMPLETE_STRIPE_ONBOARDING" -> {
                                StripeOnboardingPendingView(
                                    viewModel = viewModel,
                                    onOpenUrl = { url ->
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    },
                                )
                            }
                            "READY_TO_GO_ONLINE" -> {
                                OnboardingCompleteView(onLogout = onLogout)
                            }
                            else -> {
                                // Unknown step
                                Text(
                                    text = "Unknown onboarding step: $currentStep",
                                    modifier = Modifier.padding(16.dp),
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                // Initial state - fetch status
                LoadingView()
            }
        }
    }
}

@Composable
private fun OnboardingTopBar(
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onLogout,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
        ) {
            Text("Logout")
        }
        
        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
            )
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading onboarding status...")
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Error Loading Status",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

// Placeholder views matching iOS

@Composable
fun BackgroundCheckScreen(
    viewModel: OnboardingViewModel,
) {
    StatusScreen(
        icon = Icons.Default.CheckCircle,
        iconTint = Color(0xFF2196F3),
        title = "Background Check Required",
        message = "Please complete your background check by clicking the link sent to your email. Once completed, your application will be reviewed.",
    )
}

@Composable
fun WaitingForBackgroundCheckView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(60.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Background Check in Progress",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your background check is being processed. This usually takes 1-3 business days. We'll notify you when it's complete.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun UnderReviewView() {
    StatusScreen(
        icon = Icons.Default.Warning,
        iconTint = Color(0xFFFF9800),
        title = "Under Review",
        message = "Your background check requires additional review. Our team will reach out to you soon with next steps.",
    )
}

@Composable
fun VerificationRejectedView() {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Rejected",
            modifier = Modifier.size(60.dp),
            tint = Color.Red,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Verification Unsuccessful",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Unfortunately, we were unable to approve your driver verification. Please contact support for more information.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@thepiper.co?subject=Driver%20Verification%20Issue")
            }
            context.startActivity(intent)
        }) {
            Text("Contact Support")
        }
    }
}

@Composable
fun StripeSetupView(
    email: String,
    viewModel: OnboardingViewModel,
    onOpenUrl: (String) -> Unit,
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Payment",
            modifier = Modifier.size(60.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Payment Setup",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "To receive payments, you need to set up your Stripe account. This is required before you can start earning.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        
        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.setupStripe(email, onOpenUrl) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Set Up Payments")
            }
        }
    }
}

@Composable
fun StripeOnboardingPendingView(
    viewModel: OnboardingViewModel,
    onOpenUrl: (String) -> Unit,
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Payment",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Complete Payment Setup",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You've started setting up your Stripe account. Click below to continue where you left off.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        
        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.continueStripeOnboarding(onOpenUrl) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Continue Stripe Setup")
            }
        }
    }
}

@Composable
fun OnboardingCompleteView(
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Complete",
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFFF9800),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Pending Admin Approval",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your onboarding is complete! We're reviewing your application and will notify you once you're approved to go online.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        
        Divider(modifier = Modifier.padding(vertical = 24.dp))
        
        Text(
            text = "Need help or have questions?",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:support@thepiper.co?subject=Driver%20Onboarding%20Support")
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Email, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Contact Support")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
        ) {
            Text("Logout")
        }
    }
}

@Composable
private fun StatusScreen(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    message: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = iconTint,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
