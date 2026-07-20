package com.mes.core.designsystem.theme

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

private val LightColorScheme = lightColorScheme(
    primary = MesColor.PrimaryTeal,
    onPrimary = Color.White,
    primaryContainer = MesColor.PrimaryTealContainer,
    onPrimaryContainer = MesColor.PrimaryTeal,
    secondary = MesColor.AccentAmber,
    onSecondary = Color.White,
    secondaryContainer = MesColor.AccentAmberContainer,
    onSecondaryContainer = MesColor.AccentAmber,
    tertiary = MesColor.Success,
    onTertiary = Color.White,
    tertiaryContainer = MesColor.SuccessLight,
    background = MesColor.Surface0,
    onBackground = MesColor.Ink900,
    surface = MesColor.Surface0,
    onSurface = MesColor.Ink900,
    surfaceVariant = MesColor.Surface1,
    onSurfaceVariant = MesColor.Ink600,
    outline = MesColor.Outline,
    outlineVariant = MesColor.OutlineVariant,
    error = MesColor.Danger,
    onError = Color.White,
    errorContainer = MesColor.DangerLight,
    onErrorContainer = MesColor.Danger,
    inverseSurface = MesColor.Ink900,
    inverseOnSurface = MesColor.Surface0,
    surfaceTint = MesColor.PrimaryTeal
)

private val DarkColorScheme = darkColorScheme(
    primary = MesColor.PrimaryTealDark,
    onPrimary = MesColor.Ink900,
    primaryContainer = MesColor.PrimaryTeal,
    onPrimaryContainer = MesColor.PrimaryTealDark,
    secondary = MesColor.AccentAmberDark,
    onSecondary = MesColor.Ink900,
    secondaryContainer = MesColor.AccentAmber,
    onSecondaryContainer = MesColor.AccentAmberDark,
    tertiary = MesColor.Success,
    onTertiary = Color.White,
    tertiaryContainer = MesColor.Success,
    background = MesColor.SurfaceDark0,
    onBackground = Color.White,
    surface = MesColor.SurfaceDark0,
    onSurface = Color.White,
    surfaceVariant = MesColor.SurfaceDark1,
    onSurfaceVariant = MesColor.Ink300,
    outline = MesColor.Ink400,
    outlineVariant = MesColor.Ink600,
    error = MesColor.Danger,
    onError = Color.White,
    errorContainer = MesColor.Danger,
    onErrorContainer = Color.White,
    inverseSurface = Color.White,
    inverseOnSurface = MesColor.Ink900,
    surfaceTint = MesColor.PrimaryTealDark
)

@Composable
fun MesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MesTypography,
        content = content
    )
}
