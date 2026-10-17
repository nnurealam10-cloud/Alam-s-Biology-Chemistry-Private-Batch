package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AzureBlue,
    onPrimary = Color.White,
    primaryContainer = DeepNavy,
    onPrimaryContainer = Color(0xFFD6E4F0),
    secondary = BioTealLight,
    onSecondary = Color(0xFF003831),
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color(0xFFA7F0E0),
    tertiary = ChemAmberLight,
    onTertiary = Color(0xFF452200),
    tertiaryContainer = Color(0xFF6B3700),
    onTertiaryContainer = Color(0xFFFFDDB8),
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkBorder,
    error = ErrorRed,
    errorContainer = Color(0xFF8C0009),
    onError = Color.White,
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    primaryContainer = SoftBlueContainer,
    onPrimaryContainer = DeepNavy,
    secondary = BioTeal,
    onSecondary = Color.White,
    secondaryContainer = BioContainer,
    onSecondaryContainer = Color(0xFF003831),
    tertiary = ChemAmber,
    onTertiary = Color.White,
    tertiaryContainer = ChemContainer,
    onTertiaryContainer = Color(0xFF4E2600),
    background = OffWhite,
    onBackground = TextPrimary,
    surface = CardSurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondary,
    outline = BorderOutline,
    error = ErrorRed,
    errorContainer = ErrorContainer,
    onError = Color.White,
    onErrorContainer = Color(0xFF680005)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our tailored education brand palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
