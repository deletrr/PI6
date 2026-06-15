package com.pontolivre.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Color Scheme ──────────────────────────────────────────────────────────────

val PrimaryBlue       = Color(0xFF1565C0)
val PrimaryBlueDark   = Color(0xFF0D47A1)
val PrimaryBlueLight  = Color(0xFF42A5F5)
val SecondaryTeal     = Color(0xFF00695C)
val TertiaryAmber     = Color(0xFFF57C00)
val ErrorRed          = Color(0xFFB00020)
val SuccessGreen      = Color(0xFF2E7D32)
val WarningAmber      = Color(0xFFF9A825)
val SurfaceLight      = Color(0xFFF8FAFE)
val SurfaceDark       = Color(0xFF1A1C1E)
val BackgroundLight   = Color(0xFFFFFFFF)
val BackgroundDark    = Color(0xFF111318)

private val LightColorScheme = lightColorScheme(
    primary           = PrimaryBlue,
    onPrimary         = Color.White,
    primaryContainer  = Color(0xFFD6E4FF),
    onPrimaryContainer= Color(0xFF001947),
    secondary         = SecondaryTeal,
    onSecondary       = Color.White,
    secondaryContainer= Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary          = TertiaryAmber,
    onTertiary        = Color.White,
    tertiaryContainer = Color(0xFFFFE0B2),
    onTertiaryContainer = Color(0xFF2D1600),
    error             = ErrorRed,
    onError           = Color.White,
    errorContainer    = Color(0xFFFFDAD6),
    onErrorContainer  = Color(0xFF410002),
    background        = BackgroundLight,
    onBackground      = Color(0xFF1A1C1E),
    surface           = SurfaceLight,
    onSurface         = Color(0xFF1A1C1E),
    surfaceVariant    = Color(0xFFE1E2EC),
    onSurfaceVariant  = Color(0xFF44474F),
    outline           = Color(0xFF74777F),
)

private val DarkColorScheme = darkColorScheme(
    primary           = PrimaryBlueLight,
    onPrimary         = Color(0xFF002F6A),
    primaryContainer  = PrimaryBlueDark,
    onPrimaryContainer= Color(0xFFD6E4FF),
    secondary         = Color(0xFF80CBC4),
    onSecondary       = Color(0xFF00342F),
    secondaryContainer= Color(0xFF004D45),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary          = Color(0xFFFFCC80),
    onTertiary        = Color(0xFF452B00),
    tertiaryContainer = Color(0xFF633F00),
    onTertiaryContainer = Color(0xFFFFDDB3),
    error             = Color(0xFFFFB4AB),
    onError           = Color(0xFF690005),
    background        = BackgroundDark,
    onBackground      = Color(0xFFE2E2E9),
    surface           = SurfaceDark,
    onSurface         = Color(0xFFE2E2E9),
    surfaceVariant    = Color(0xFF44474F),
    onSurfaceVariant  = Color(0xFFC4C6D0),
    outline           = Color(0xFF8E9099),
)

@Composable
fun PontoLivreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

val AppTypography = Typography()
val AppShapes = Shapes()

// ── Status colors ─────────────────────────────────────────────────────────────

@Composable
fun statusColor(status: String): Color = when (status.uppercase()) {
    "FREE"       -> SuccessGreen
    "OCCUPIED"   -> ErrorRed
    "RESERVED"   -> WarningAmber
    "MAINTENANCE"-> Color(0xFF9E9E9E)
    else         -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun transactionColor(type: String): Color = when (type) {
    "CREDIT_PIX", "CREDIT_CARD" -> SuccessGreen
    "DEBIT_SESSION", "DEBIT_FINE" -> ErrorRed
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
