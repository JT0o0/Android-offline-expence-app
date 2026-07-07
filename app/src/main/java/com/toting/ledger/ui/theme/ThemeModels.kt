package com.toting.ledger.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Light/dark behaviour selected by the user. */
enum class DarkMode { SYSTEM, LIGHT, DARK }

/**
 * The full theme state persisted in DataStore. [presetId] picks a base palette;
 * the nullable *Override fields let the user tweak individual key colors on top.
 * [backgroundImagePath] points to a gallery photo copied into app storage, shown
 * behind all tab pages (null = the default gradient background).
 */
data class ThemeState(
    val presetId: String = ThemePresets.DEFAULT_ID,
    val darkMode: DarkMode = DarkMode.SYSTEM,
    val primaryOverride: Int? = null,
    val secondaryOverride: Int? = null,
    val backgroundOverride: Int? = null,
    val incomeOverride: Int? = null,
    val expenseOverride: Int? = null,
    val backgroundImagePath: String? = null,
    val glassAlpha: Float = DEFAULT_GLASS_ALPHA,
)

/** Absolute path of the user's custom background photo, provided by AppTheme. */
val LocalBackgroundImagePath = staticCompositionLocalOf<String?> { null }

/** Default glass tint alpha — matches HazeMaterials.thin's light tint, so the default look is unchanged. */
const val DEFAULT_GLASS_ALPHA = 0.6f

/**
 * User-tuned glass opacity, provided by AppTheme. Deliberately NOT a static local:
 * the value changes continuously while the settings slider is dragged, and fine-grained
 * invalidation keeps recomposition limited to the actual readers.
 */
val LocalGlassAlpha = compositionLocalOf { DEFAULT_GLASS_ALPHA }

/** Semantic colors Material 3 doesn't provide. Supplied via [LocalAppColors]. */
data class AppColors(
    val income: Color,
    val expense: Color,
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(income = IncomeGreen, expense = ExpenseRed)
}

/** Accessor for app-specific colors, mirroring how `MaterialTheme.colorScheme` is used. */
object Ledger {
    val colors: AppColors
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current
}
