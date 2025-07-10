package com.piperrideshare.driver.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.piperrideshare.driver.ui.screens.home.HomeScreen
import com.piperrideshare.driver.ui.screens.login.LoginScreen
import com.piperrideshare.driver.ui.screens.splash.SplashScreen

object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
    const val RIDE_DETAIL = "ride_detail/{rideId}"
    const val ACCOUNT = "account"
    const val EARNINGS = "earnings"
    const val SETTINGS = "settings"
}

@Composable
fun PiperDriverNavHost(navController: NavHostController) {
    val actions = remember(navController) { NavActions(navController) }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH,
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = actions.navigateToLogin,
                onNavigateToHome = actions.navigateToHome,
            )
        }

        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = actions.navigateToHome,
            )
        }

        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToRideDetail = actions.navigateToRideDetail,
            )
        }

        // Additional routes
    }
}

class NavActions(
    private val navController: NavHostController,
) {
    val navigateToLogin: () -> Unit = {
        navController.navigate(NavRoutes.LOGIN) {
            popUpTo(NavRoutes.SPLASH) { inclusive = true }
        }
    }

    val navigateToHome: () -> Unit = {
        navController.navigate(NavRoutes.HOME) {
            popUpTo(NavRoutes.LOGIN) { inclusive = true }
        }
    }

    val navigateToRideDetail: (String) -> Unit = { rideId ->
        navController.navigate("ride_detail/$rideId")
    }

    // Additional navigation actions
}
