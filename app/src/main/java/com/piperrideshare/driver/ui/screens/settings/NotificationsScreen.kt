package com.piperrideshare.driver.ui.screens.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF3F0F5),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Notifications",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Tell us the types of push notifications you would like to get on this device. You can change this at any time.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Notification Preference Items
            NotificationPreferenceItem(
                title = "New Ride Requests",
                subtitle = "Receive notifications for available rides and pickup requests in your area."
            )
            
            NotificationPreferenceItem(
                title = "Promotions & Incentives",
                subtitle = "Get notified about surge pricing, bonuses, and special earning opportunities."
            )
            
            NotificationPreferenceItem(
                title = "Account & Security",
                subtitle = "Important alerts about your account status, document expirations, and security."
            )
            
            NotificationPreferenceItem(
                title = "Messages from Riders",
                subtitle = "Receive alerts when a rider sends you a message during an active trip."
            )
            
            NotificationPreferenceItem(
                title = "Earnings & Tips",
                subtitle = "Get notified as soon as a payment is processed or when you receive a tip."
            )
            
            NotificationPreferenceItem(
                title = "App Updates & News",
                subtitle = "Information about new features, service area changes, or important announcements.",
                isLast = true
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun NotificationPreferenceItem(
    title: String,
    subtitle: String,
    isLast: Boolean = false
) {
    var isEnabled by remember { mutableStateOf(true) }
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Switch(
                checked = isEnabled,
                onCheckedChange = { isEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color.Black,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.LightGray,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
        
        if (!isLast) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
            )
        }
    }
}
