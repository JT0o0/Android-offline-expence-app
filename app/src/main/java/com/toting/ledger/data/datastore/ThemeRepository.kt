package com.toting.ledger.data.datastore

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.toting.ledger.ui.theme.DarkMode
import com.toting.ledger.ui.theme.ThemePresets
import com.toting.ledger.ui.theme.ThemeState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

/** Persists the theme/color customization in DataStore. */
@Singleton
class ThemeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val ds = context.themeDataStore

    val themeState: Flow<ThemeState> = ds.data.map { p ->
        ThemeState(
            presetId = p[KEY_PRESET] ?: ThemePresets.DEFAULT_ID,
            darkMode = p[KEY_DARK]?.let { runCatching { DarkMode.valueOf(it) }.getOrNull() } ?: DarkMode.SYSTEM,
            primaryOverride = p[KEY_PRIMARY],
            secondaryOverride = p[KEY_SECONDARY],
            backgroundOverride = p[KEY_BACKGROUND],
            incomeOverride = p[KEY_INCOME],
            expenseOverride = p[KEY_EXPENSE],
            backgroundImagePath = p[KEY_BG_IMAGE],
        )
    }

    suspend fun setPreset(id: String) = ds.edit { it[KEY_PRESET] = id }
    suspend fun setDarkMode(mode: DarkMode) = ds.edit { it[KEY_DARK] = mode.name }

    suspend fun setPrimaryOverride(color: Int?) = setOrRemove(KEY_PRIMARY, color)
    suspend fun setSecondaryOverride(color: Int?) = setOrRemove(KEY_SECONDARY, color)
    suspend fun setBackgroundOverride(color: Int?) = setOrRemove(KEY_BACKGROUND, color)
    suspend fun setIncomeOverride(color: Int?) = setOrRemove(KEY_INCOME, color)
    suspend fun setExpenseOverride(color: Int?) = setOrRemove(KEY_EXPENSE, color)

    /** Clears all per-color overrides (keeps the selected preset & dark mode). */
    suspend fun resetColors() = ds.edit {
        it.remove(KEY_PRIMARY); it.remove(KEY_SECONDARY); it.remove(KEY_BACKGROUND)
        it.remove(KEY_INCOME); it.remove(KEY_EXPENSE)
    }

    /**
     * Copies the picked gallery photo into app storage (a unique file per pick, so the
     * UI's path-keyed bitmap cache invalidates) and remembers it as the background.
     */
    suspend fun setBackgroundImage(uri: Uri) = withContext(Dispatchers.IO) {
        val old = ds.data.first()[KEY_BG_IMAGE]
        val file = File(context.filesDir, "background_${System.currentTimeMillis()}.img")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: run {
            file.delete()
            error("無法讀取所選的圖片")
        }
        ds.edit { it[KEY_BG_IMAGE] = file.absolutePath }
        old?.let { File(it).delete() }
    }

    suspend fun clearBackgroundImage() = withContext(Dispatchers.IO) {
        val old = ds.data.first()[KEY_BG_IMAGE]
        ds.edit { it.remove(KEY_BG_IMAGE) }
        old?.let { File(it).delete() }
    }

    private suspend fun setOrRemove(key: Preferences.Key<Int>, color: Int?) = ds.edit {
        if (color == null) it.remove(key) else it[key] = color
    }

    private companion object {
        val KEY_PRESET = stringPreferencesKey("preset")
        val KEY_DARK = stringPreferencesKey("dark_mode")
        val KEY_PRIMARY = intPreferencesKey("primary")
        val KEY_SECONDARY = intPreferencesKey("secondary")
        val KEY_BACKGROUND = intPreferencesKey("background")
        val KEY_INCOME = intPreferencesKey("income")
        val KEY_EXPENSE = intPreferencesKey("expense")
        val KEY_BG_IMAGE = stringPreferencesKey("bg_image_path")
    }
}
