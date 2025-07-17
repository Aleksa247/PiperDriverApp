package com.piperrideshare.driver.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.piperrideshare.driver.ui.screens.home.HomeScreen
import com.piperrideshare.driver.ui.screens.login.LoginScreen
import com.piperrideshare.driver.ui.screens.splash.SplashScreen

/**
 * Navigation Routes - Centralized route definitions for the driver app
 *
 * This object contains all the route constants used throughout the app for navigation.
 * Centralizing routes here makes it easier to maintain and update navigation paths.
 *
 * @author Thomas Woodfin
 */
object NavRoutes {
    const val SPLASH = "splash" // Initial loading screen
    const val LOGIN = "login" // Authentication screen
    const val HOME = "home" // Main driver dashboard
    const val RIDE_DETAIL = "ride_detail/{rideId}" // Individual ride details
    const val ACCOUNT = "account" // Driver account management
    const val EARNINGS = "earnings" // Earnings and payment history
    const val SETTINGS = "settings" // App settings and preferences
}

/**
 * PiperDriverNavHost - Main navigation container for the driver app
 *
 * This composable manages the navigation between different screens in the app.
 * It defines the navigation graph and handles screen transitions.
 *
 * Navigation Flow:
 * 1. Splash Screen (initial loading)
 * 2. Login Screen (authentication)
 * 3. Home Screen (main driver interface)
 *
 * @param navController The navigation controller that manages screen navigation
 * @author Thomas Woodfin
 */
@Composable
fun PiperDriverNavHost(navController: NavHostController) {
    // Create navigation actions that can be passed to screens
    val actions = remember(navController) { NavActions(navController) }

    // Define the navigation graph
    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH, // App starts with splash screen
    ) {
        // Splash screen - shown during app initialization
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = actions.navigateToLogin,
            )
        }

        // Login screen - handles user authentication
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = actions.navigateToHome,
            )
        }

        // Home screen - main driver interface with ride management
        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToRideDetail = actions.navigateToRideDetail,
                onLogout = actions.navigateToLoginAfterLogout,
            )
        }
    }
}

/**
 * NavActions - Navigation action handlers for the driver app
 *
 * This class encapsulates all navigation logic and provides type-safe navigation methods
 * that can be passed to different screens. It handles the navigation stack management
 * and ensures proper screen transitions.
 *
 * @param navController The navigation controller instance
 * @author Thomas Woodfin
 */
class NavActions(
    private val navController: NavHostController,
) {
    /**
     * Navigate to login screen and clear the back stack
     * Used when user needs to authenticate
     */
    val navigateToLogin: () -> Unit = {
        navController.navigate(NavRoutes.LOGIN) {
            // Clear the back stack up to splash screen (inclusive)
            popUpTo(NavRoutes.SPLASH) { inclusive = true }
        }
    }

    /**
     * Navigate to login screen after logout
     * Clears the entire navigation stack to prevent back navigation
     */
    val navigateToLoginAfterLogout: () -> Unit = {
        navController.navigate(NavRoutes.LOGIN) {
            // Clear the entire back stack
            popUpTo(0) { inclusive = true }
        }
    }

    /**
     * Navigate to home screen after successful login
     * Removes login screen from back stack to prevent going back to login
     */
    val navigateToHome: () -> Unit = {
        navController.navigate(NavRoutes.HOME) {
            // Clear the back stack up to login screen (inclusive)
            popUpTo(NavRoutes.LOGIN) { inclusive = true }
        }
    }

    /**
     * Navigate to ride detail screen with specific ride ID
     * @param rideId The unique identifier of the ride to display
     */
    val navigateToRideDetail: (String) -> Unit = { rideId ->
        navController.navigate("ride_detail/$rideId")
    }
}
