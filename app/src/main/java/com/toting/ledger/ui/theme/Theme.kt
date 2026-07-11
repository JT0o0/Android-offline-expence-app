package com.toting.ledger.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Themed root. Reads the persisted [ThemeState] from DataStore (via [ThemeViewModel]),
 * builds a Material 3 [ColorScheme] from the chosen preset + user color overrides, and
 * exposes the income/expense semantic colors through [LocalAppColors]. Changes apply
 * instantly and survive restarts.
 */
@Composable
fun AppTheme(
    viewModel: ThemeViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    AppTheme(state = state, content = content)
}

@Composable
fun AppTheme(
    state: ThemeState,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (state.darkMode) {
        DarkMode.SYSTEM -> systemDark
        DarkMode.LIGHT -> false
        DarkMode.DARK -> true
    }
    val preset = ThemePresets.byId(state.presetId)
    val base = if (dark) preset.dark else preset.light
    // Keyed on the color-relevant fields only — keying on the whole state would rebuild
    // the ColorScheme (and recompose the entire MaterialTheme subtree) on every tick of
    // the glass-opacity slider drag.
    val colorScheme = remember(
        state.presetId, state.primaryOverride, state.secondaryOverride, state.backgroundOverride, dark,
    ) { applyOverrides(base, state) }

    val income = state.incomeOverride?.let { Color(it) } ?: if (dark) IncomeGreenDark else IncomeGreen
    val expense = state.expenseOverride?.let { Color(it) } ?: if (dark) ExpenseRedDark else ExpenseRed

    // Status/navigation bar icon appearance must follow the APP theme, not the system
    // one (enableEdgeToEdge's default): system-dark + app-light would leave white icons
    // on a light background. Actual background luminance (not the dark flag) also keeps
    // icons visible when the user overrides the background with a dark color.
    val view = LocalView.current
    if (!view.isInEditMode) {
        val lightBackground = colorScheme.background.luminance() > 0.5f
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = lightBackground
            controller.isAppearanceLightNavigationBars = lightBackground
        }
    }

    CompositionLocalProvider(
        LocalAppColors provides AppColors(income = income, expense = expense),
        LocalBackgroundImagePath provides state.backgroundImagePath,
        LocalGlassAlpha provides state.glassAlpha,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LedgerTypography,
            shapes = LedgerShapes,
            content = content,
        )
    }
}

/** Applies the user's key-color overrides on top of the preset scheme, keeping text legible. */
private fun applyOverrides(base: ColorScheme, state: ThemeState): ColorScheme {
    val primary = state.primaryOverride?.let { Color(it) }
    val secondary = state.secondaryOverride?.let { Color(it) }
    val background = state.backgroundOverride?.let { Color(it) }
    return base.copy(
        primary = primary ?: base.primary,
        onPrimary = primary?.let(::onColorFor) ?: base.onPrimary,
        secondary = secondary ?: base.secondary,
        onSecondary = secondary?.let(::onColorFor) ?: base.onSecondary,
        background = background ?: base.background,
        onBackground = background?.let(::onColorFor) ?: base.onBackground,
        surface = background ?: base.surface,
        onSurface = background?.let(::onColorFor) ?: base.onSurface,
    )
}

private fun onColorFor(c: Color): Color =
    if (c.luminance() > 0.5f) Color(0xFF1A1A1A) else Color.White

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
