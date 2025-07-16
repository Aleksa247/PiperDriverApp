package com.piperrideshare.driver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.piperrideshare.driver.ui.navigation.PiperDriverNavHost
import com.piperrideshare.driver.ui.theme.DriverAppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - Primary entry point for the Piper Driver App
 *
 * This activity serves as the main container for the entire driver application.
 * It initializes the Jetpack Compose UI framework and sets up the navigation system.
 *
 * Key responsibilities:
 * - Initializes the app's UI using Jetpack Compose
 * - Sets up the Material Design 3 theme
 * - Creates and manages the navigation controller
 * - Integrates with Hilt for dependency injection
 *
 * @author Thomas Woodfin
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the Compose UI content
        setContent {
            // Apply the custom theme for the driver app
            DriverAppTheme {
                // Create a surface that fills the entire screen
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    // Initialize navigation controller for screen navigation
                    val navController = rememberNavController()

                    // Set up the navigation host that manages all app screens
                    PiperDriverNavHost(navController = navController)
                }
            }
        }
    }
}
