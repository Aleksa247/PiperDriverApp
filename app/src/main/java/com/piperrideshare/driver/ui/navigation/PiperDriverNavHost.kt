package com.piperrideshare.driver.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.piperrideshare.driver.BuildConfig
import com.piperrideshare.driver.ui.screens.debug.DebugMenuScreen
import com.piperrideshare.driver.ui.screens.home.HomeScreen
import com.piperrideshare.driver.ui.screens.login.LoginScreen
import com.piperrideshare.driver.ui.screens.onboarding.OnboardingCoordinator
import com.piperrideshare.driver.ui.screens.splash.SplashScreen
import com.piperrideshare.driver.ui.screens.chat.ChatScreen

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
    const val ONBOARDING = "onboarding/{email}/{phone}" // Onboarding flow (verification, background check, Stripe)
    const val HOME = "home" // Main driver dashboard
    const val RIDE_DETAIL = "ride_detail/{rideId}" // Individual ride details
    const val ACCOUNT = "account" // Driver account management
    const val EARNINGS = "earnings" // Earnings and payment history
    const val SETTINGS = "settings" // App settings and preferences
    const val DEBUG_MENU = "debug_menu" // Debug settings (DEBUG builds only)
    const val CHAT = "chat/{rideId}" // Chat screen for specific ride
    const val REGISTER = "register" // Driver registration screen

    fun onboarding(email: String, phone: String): String = 
        "onboarding/${Uri.encode(email)}/${Uri.encode(phone)}"
        
    fun chat(rideId: String): String = "chat/$rideId"
}

private object Uri {
    fun encode(value: String): String = java.net.URLEncoder.encode(value, "UTF-8")
    fun decode(value: String): String = java.net.URLDecoder.decode(value, "UTF-8")
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
 * 3. Onboarding Screen (verification & setup - if needed)
 * 4. Home Screen (main driver interface)
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
                onNavigateToHome = actions.navigateToHome,
            )
        }

        // Login screen - handles user authentication
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = actions.navigateToHome, // For returning users, go straight home
                onLoginSuccessWithOnboarding = actions.navigateToOnboarding, // For new users, go to onboarding
                onNavigateToDebugMenu = if (BuildConfig.DEBUG) actions.navigateToDebugMenu else null,
                onNavigateToRegister = actions.navigateToRegister,
            )
        }

        // Register screen
        composable(NavRoutes.REGISTER) {
            com.piperrideshare.driver.ui.screens.register.RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = actions.navigateToHome,
            )
        }

        // Debug menu - only available in DEBUG builds
        if (BuildConfig.DEBUG) {
            composable(NavRoutes.DEBUG_MENU) {
                DebugMenuScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // Onboarding flow - verification, background check, Stripe setup
        composable(NavRoutes.ONBOARDING) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email")?.let { Uri.decode(it) } ?: ""
            val phone = backStackEntry.arguments?.getString("phone")?.let { Uri.decode(it) } ?: ""
            
            OnboardingCoordinator(
                email = email,
                phone = phone,
                onComplete = actions.navigateToHomeFromOnboarding,
                onLogout = actions.navigateToLoginAfterLogout,
            )
        }

        // Home screen - main driver interface with ride management
        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToRideDetail = actions.navigateToRideDetail,
                onLogout = actions.navigateToLoginAfterLogout,
                onNavigateToChat = actions.navigateToChat,
            )
        }
        
        // Chat screen
        composable(NavRoutes.CHAT) { backStackEntry ->
            val rideId = backStackEntry.arguments?.getString("rideId") ?: ""
            ChatScreen(
                rideId = rideId,
                onBackClick = { navController.popBackStack() }
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
     * Navigate to register screen
     */
    val navigateToRegister: () -> Unit = {
        navController.navigate(NavRoutes.REGISTER)
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
     * Navigate to onboarding screen after login (for new users)
     * @param email Driver's email for verification
     * @param phone Driver's phone for verification
     */
    val navigateToOnboarding: (email: String, phone: String) -> Unit = { email, phone ->
        navController.navigate(NavRoutes.onboarding(email, phone)) {
            // Clear the back stack up to login screen (inclusive)
            popUpTo(NavRoutes.LOGIN) { inclusive = true }
        }
    }

    /**
     * Navigate to home screen from onboarding (when onboarding is complete)
     */
    val navigateToHomeFromOnboarding: () -> Unit = {
        navController.navigate(NavRoutes.HOME) {
            // Clear onboarding from back stack
            popUpTo(0) { inclusive = true }
        }
    }

    /**
     * Navigate to ride detail screen with specific ride ID
     * @param rideId The unique identifier of the ride to display
     */
    val navigateToRideDetail: (String) -> Unit = { rideId ->
        navController.navigate("ride_detail/$rideId")
    }

    /**
     * Navigate to debug menu (DEBUG builds only)
     */
    val navigateToDebugMenu: () -> Unit = {
        navController.navigate(NavRoutes.DEBUG_MENU)
    }

    /**
     * Navigate to chat screen for a specific ride
     */
    val navigateToChat: (String) -> Unit = { rideId ->
        navController.navigate(NavRoutes.chat(rideId))
    }
}

