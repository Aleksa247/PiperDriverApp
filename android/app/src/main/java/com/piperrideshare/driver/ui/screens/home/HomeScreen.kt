package com.piperrideshare.driver.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(
    onNavigateToRideDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    var isOnline by remember { mutableStateOf(false) }
    var earnings by remember { mutableStateOf(0.0) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Driver App",
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "$${String.format("%.2f", earnings)} Earned",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { isOnline = !isOnline },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isOnline) "Go Offline" else "Go Online")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isOnline) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "Status: Online",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Waiting for ride requests...",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
