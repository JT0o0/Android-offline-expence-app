package com.toting.ledger.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toting.ledger.data.backup.BackupManager
import com.toting.ledger.data.datastore.ThemeRepository
import com.toting.ledger.ui.theme.DarkMode
import com.toting.ledger.ui.theme.ThemeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Which key color is currently being edited in the swatch dialog. */
enum class ColorTarget { PRIMARY, SECONDARY, BACKGROUND, INCOME, EXPENSE }

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val backupManager: BackupManager,
) : ViewModel() {

    val themeState: StateFlow<ThemeState> = themeRepository.themeState
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeState())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun consumeMessage() { _message.value = null }

    fun setPreset(id: String) = launchIo { themeRepository.setPreset(id) }
    fun setDarkMode(mode: DarkMode) = launchIo { themeRepository.setDarkMode(mode) }

    fun setColor(target: ColorTarget, color: Int?) = launchIo {
        when (target) {
            ColorTarget.PRIMARY -> themeRepository.setPrimaryOverride(color)
            ColorTarget.SECONDARY -> themeRepository.setSecondaryOverride(color)
            ColorTarget.BACKGROUND -> themeRepository.setBackgroundOverride(color)
            ColorTarget.INCOME -> themeRepository.setIncomeOverride(color)
            ColorTarget.EXPENSE -> themeRepository.setExpenseOverride(color)
        }
    }

    fun resetColors() = launchIo { themeRepository.resetColors() }

    // onValueChangeFinished carries no value, so the last previewed value is kept here.
    private var pendingGlassAlpha: Float? = null

    /** Live in-memory preview while the slider drags — no disk writes. */
    fun previewGlassAlpha(value: Float) {
        pendingGlassAlpha = value
        themeRepository.previewGlassAlpha(value)
    }

    /** Persists the last previewed glass opacity when the drag ends. */
    fun commitGlassAlpha() = launchIo {
        pendingGlassAlpha?.let { themeRepository.setGlassAlpha(it) }
        pendingGlassAlpha = null
    }

    fun setBackgroundImage(uri: Uri) =
        runReporting("設定背景") { themeRepository.setBackgroundImage(uri); "已套用背景圖片" }

    fun clearBackgroundImage() =
        runReporting("清除背景") { themeRepository.clearBackgroundImage(); "已清除背景圖片" }

    fun exportCsv(uri: Uri) = runReporting("匯出") { backupManager.exportCsv(uri).let { "已匯出 $it 筆紀錄" } }
    fun backup(uri: Uri) = runReporting("備份") { backupManager.backup(uri); "備份完成" }
    fun restore(uri: Uri) = runReporting("還原") { backupManager.restore(uri).let { "已還原 $it 筆紀錄" } }

    private fun runReporting(action: String, block: suspend () -> String) {
        viewModelScope.launch {
            _message.value = runCatching { block() }.getOrElse { "$action 失敗：${it.message}" }
        }
    }

    private fun launchIo(block: suspend () -> Unit) = viewModelScope.launch { block() }
}
