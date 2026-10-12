package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TruthTraceDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF042622),
    primaryContainer = Color(0xFF133E39),
    onPrimaryContainer = ElectricCyan,
    secondary = RestrainedViolet,
    onSecondary = Color(0xFF221B47),
    secondaryContainer = Color(0xFF332961),
    onSecondaryContainer = Color(0xFFD6CFFF),
    tertiary = AccessibleTeal,
    onTertiary = Color(0xFF003731),
    background = MidnightNavy,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    outlineVariant = Color(0xFF1E2D48),
    error = CoralError,
    onError = Color(0xFF450A0A)
)

@Composable
fun TruthTraceTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TruthTraceDarkColorScheme,
        typography = Typography,
        content = content
    )
}
