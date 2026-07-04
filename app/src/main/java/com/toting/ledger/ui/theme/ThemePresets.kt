package com.toting.ledger.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Shared semantic income/expense colors (overridable by the user). */
val IncomeGreenDark = Color(0xFF7FD6A0)
val ExpenseRedDark = Color(0xFFFF8A80)

/** A selectable base palette with a coordinated light and dark scheme. */
data class ThemePreset(
    val id: String,
    val displayName: String,
    val light: ColorScheme,
    val dark: ColorScheme,
)

object ThemePresets {
    const val DEFAULT_ID = "sage"

    val all: List<ThemePreset> = listOf(
        ThemePreset("sage", "沉穩綠", SageLight, SageDark),
        ThemePreset("indigo", "靛藍", IndigoLight, IndigoDark),
        ThemePreset("sunset", "暖橘", SunsetLight, SunsetDark),
        ThemePreset("mono", "純淨", MonoLight, MonoDark),
    )

    fun byId(id: String): ThemePreset = all.firstOrNull { it.id == id } ?: all.first()
}

// ---------------- Sage (default) ----------------
private val SageLight = lightColorScheme(
    primary = LightPrimary, onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer, onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary, onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer, onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary, onTertiary = LightOnTertiary,
    background = LightBackground, onBackground = LightOnBackground,
    surface = LightSurface, onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant, onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline, error = LightError, onError = LightOnError,
)
private val SageDark = darkColorScheme(
    primary = DarkPrimary, onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer, onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary, onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer, onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary, onTertiary = DarkOnTertiary,
    background = DarkBackground, onBackground = DarkOnBackground,
    surface = DarkSurface, onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant, onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline, error = DarkError, onError = DarkOnError,
)

// ---------------- Indigo ----------------
private val IndigoLight = lightColorScheme(
    primary = Color(0xFF4356C7), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDE1FF), onPrimaryContainer = Color(0xFF00164F),
    secondary = Color(0xFF5A5D72), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDFE0F9), onSecondaryContainer = Color(0xFF171A2C),
    tertiary = Color(0xFF76547D), onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFEFBFF), onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFEFBFF), onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE2E1EC), onSurfaceVariant = Color(0xFF45464F),
    outline = Color(0xFF767680),
)
private val IndigoDark = darkColorScheme(
    primary = Color(0xFFBCC2FF), onPrimary = Color(0xFF0A2391),
    primaryContainer = Color(0xFF2A3FAF), onPrimaryContainer = Color(0xFFDDE1FF),
    secondary = Color(0xFFC3C4DD), onSecondary = Color(0xFF2C2F42),
    secondaryContainer = Color(0xFF424559), onSecondaryContainer = Color(0xFFDFE0F9),
    tertiary = Color(0xFFE4BAE8), onTertiary = Color(0xFF432650),
    background = Color(0xFF1B1B1F), onBackground = Color(0xFFE4E1E6),
    surface = Color(0xFF131316), onSurface = Color(0xFFE4E1E6),
    surfaceVariant = Color(0xFF45464F), onSurfaceVariant = Color(0xFFC6C5D0),
    outline = Color(0xFF90909A),
)

// ---------------- Sunset (warm) ----------------
private val SunsetLight = lightColorScheme(
    primary = Color(0xFF8B5000), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDCC2), onPrimaryContainer = Color(0xFF2D1600),
    secondary = Color(0xFF755846), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDCC2), onSecondaryContainer = Color(0xFF2B1709),
    tertiary = Color(0xFF5F6135), onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFBFF), onBackground = Color(0xFF201A17),
    surface = Color(0xFFFFFBFF), onSurface = Color(0xFF201A17),
    surfaceVariant = Color(0xFFF3DED1), onSurfaceVariant = Color(0xFF52443B),
    outline = Color(0xFF85746B),
)
private val SunsetDark = darkColorScheme(
    primary = Color(0xFFFFB77C), onPrimary = Color(0xFF4A2800),
    primaryContainer = Color(0xFF6A3C00), onPrimaryContainer = Color(0xFFFFDCC2),
    secondary = Color(0xFFE5BFA9), onSecondary = Color(0xFF422B1B),
    secondaryContainer = Color(0xFF5B4130), onSecondaryContainer = Color(0xFFFFDCC2),
    tertiary = Color(0xFFC9CA95), onTertiary = Color(0xFF31320B),
    background = Color(0xFF201A17), onBackground = Color(0xFFECE0DA),
    surface = Color(0xFF181210), onSurface = Color(0xFFECE0DA),
    surfaceVariant = Color(0xFF52443B), onSurfaceVariant = Color(0xFFD7C2B8),
    outline = Color(0xFF9F8D82),
)

// ---------------- Mono (minimal) ----------------
private val MonoLight = lightColorScheme(
    primary = Color(0xFF4A4A4A), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE2E2E2), onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = Color(0xFF5E5E5E), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE6E6E6), onSecondaryContainer = Color(0xFF1B1B1B),
    tertiary = Color(0xFF4A4A4A), onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFAFAFA), onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFFFF), onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFE3E3E3), onSurfaceVariant = Color(0xFF474747),
    outline = Color(0xFF787878),
)
private val MonoDark = darkColorScheme(
    primary = Color(0xFFC6C6C6), onPrimary = Color(0xFF303030),
    primaryContainer = Color(0xFF474747), onPrimaryContainer = Color(0xFFE2E2E2),
    secondary = Color(0xFFC7C7C7), onSecondary = Color(0xFF303030),
    secondaryContainer = Color(0xFF474747), onSecondaryContainer = Color(0xFFE6E6E6),
    tertiary = Color(0xFFC6C6C6), onTertiary = Color(0xFF303030),
    background = Color(0xFF121212), onBackground = Color(0xFFE2E2E2),
    surface = Color(0xFF1A1A1A), onSurface = Color(0xFFE2E2E2),
    surfaceVariant = Color(0xFF474747), onSurfaceVariant = Color(0xFFC8C8C8),
    outline = Color(0xFF929292),
)
