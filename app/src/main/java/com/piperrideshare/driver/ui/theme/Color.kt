package com.piperrideshare.driver.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    // Main colors
    val Primary = Color(0xFF000000) // System primary (black)
    val Secondary = Color(0xFF757575) // System secondary (gray)

    // Accent colors
    val Blue = Color(0xFF57A9FB) // The specific blue used for route lines
    val Orange = Color(0xFFFFA500) // Used for location markers
    val Green = Color(0xFF4CAF50) // Used for positive actions
    val Red = Color(0xFFF44336) // Used for warnings/errors

    // Background colors
    val Background = Color(0xFFFFFFFF) // White
    val Surface = Color(0xFFFFFFFF) // White

    // Text colors
    val OnPrimary = Color(0xFFFFFFFF) // White text on primary
    val OnSecondary = Color(0xFF000000) // Black text on secondary
    val OnBackground = Color(0xFF000000) // Black text on background
    val OnSurface = Color(0xFF000000) // Black text on surface

    // Utility functions for opacity
    fun blackWithOpacity(opacity: Float) = Color(0xFF000000).copy(alpha = opacity)

    fun orangeWithOpacity(opacity: Float) = Orange.copy(alpha = opacity)

    fun redWithOpacity(opacity: Float) = Red.copy(alpha = opacity)

    fun whiteWithOpacity(opacity: Float) = Color(0xFFFFFFFF).copy(alpha = opacity)

    fun greenWithOpacity(opacity: Float) = Green.copy(alpha = opacity)

    fun primaryWithOpacity(opacity: Float) = Primary.copy(alpha = opacity)
}
