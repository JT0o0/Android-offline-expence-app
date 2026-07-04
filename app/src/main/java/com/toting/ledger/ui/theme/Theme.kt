package com.toting.ledger.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
    val colorScheme = remember(state, dark) { applyOverrides(base, state) }

    val income = state.incomeOverride?.let { Color(it) } ?: if (dark) IncomeGreenDark else IncomeGreen
    val expense = state.expenseOverride?.let { Color(it) } ?: if (dark) ExpenseRedDark else ExpenseRed

    CompositionLocalProvider(
        LocalAppColors provides AppColors(income = income, expense = expense),
        LocalBackgroundImagePath provides state.backgroundImagePath,
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
