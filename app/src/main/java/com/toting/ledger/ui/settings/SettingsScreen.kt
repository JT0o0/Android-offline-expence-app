package com.toting.ledger.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toting.ledger.ui.components.LocalHazeState
import com.toting.ledger.ui.components.colorpicker.ColorPickerDialog
import com.toting.ledger.ui.components.glassSource
import com.toting.ledger.ui.navigation.LocalBottomBarPadding
import com.toting.ledger.ui.theme.DarkMode
import com.toting.ledger.ui.theme.ExpenseRed
import com.toting.ledger.ui.theme.IncomeGreen
import com.toting.ledger.ui.theme.ThemePresets
import com.toting.ledger.ui.theme.ThemeState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onOpenCategories: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val theme by viewModel.themeState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var colorTarget by remember { mutableStateOf<ColorTarget?>(null) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    val dateStamp = remember { LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let(viewModel::exportCsv) }
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(viewModel::backup) }
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::restore) }
    val bgImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(viewModel::setBackgroundImage) }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("設定") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                // This page's content is the blur backdrop for the glass bottom bar.
                .glassSource(LocalHazeState.current)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("外觀")

            SettingLabel("主題")
            FlowRow(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemePresets.all.forEach { preset ->
                    FilterChip(
                        selected = theme.presetId == preset.id,
                        onClick = { viewModel.setPreset(preset.id) },
                        label = { Text(preset.displayName) },
                    )
                }
            }

            SettingLabel("深淺模式")
            FlowRow(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DarkModeChip("跟隨系統", DarkMode.SYSTEM, theme.darkMode, viewModel::setDarkMode)
                DarkModeChip("淺色", DarkMode.LIGHT, theme.darkMode, viewModel::setDarkMode)
                DarkModeChip("深色", DarkMode.DARK, theme.darkMode, viewModel::setDarkMode)
            }

            SettingLabel("自訂顏色")
            ColorRow("主色", currentColor(theme, ColorTarget.PRIMARY)) { colorTarget = ColorTarget.PRIMARY }
            ColorRow("輔色", currentColor(theme, ColorTarget.SECONDARY)) { colorTarget = ColorTarget.SECONDARY }
            ColorRow("背景", currentColor(theme, ColorTarget.BACKGROUND)) { colorTarget = ColorTarget.BACKGROUND }
            ColorRow("收入色", currentColor(theme, ColorTarget.INCOME)) { colorTarget = ColorTarget.INCOME }
            ColorRow("支出色", currentColor(theme, ColorTarget.EXPENSE)) { colorTarget = ColorTarget.EXPENSE }
            TextButton(
                onClick = viewModel::resetColors,
                modifier = Modifier.padding(start = 8.dp),
            ) { Text("重設所有顏色") }

            SettingLabel("背景圖片")
            ListItem(
                modifier = Modifier.clickable {
                    bgImageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                leadingContent = { Icon(Icons.Filled.Wallpaper, null) },
                headlineContent = { Text("從圖庫選擇") },
                supportingContent = {
                    Text(if (theme.backgroundImagePath != null) "已套用自訂背景" else "未設定")
                },
                trailingContent = if (theme.backgroundImagePath != null) {
                    { TextButton(onClick = viewModel::clearBackgroundImage) { Text("清除") } }
                } else null,
            )

            SectionHeader("管理")
            ListItem(
                modifier = Modifier.clickable(onClick = onOpenCategories),
                leadingContent = { Icon(Icons.Filled.Category, null) },
                headlineContent = { Text("分類管理") },
            )

            SectionHeader("資料")
            ListItem(
                modifier = Modifier.clickable { exportLauncher.launch("ledger-$dateStamp.csv") },
                leadingContent = { Icon(Icons.Filled.FileDownload, null) },
                headlineContent = { Text("匯出 CSV") },
                supportingContent = { Text("匯出所有紀錄為試算表") },
            )
            ListItem(
                modifier = Modifier.clickable { backupLauncher.launch("ledger-backup-$dateStamp.json") },
                leadingContent = { Icon(Icons.Filled.Save, null) },
                headlineContent = { Text("備份資料") },
                supportingContent = { Text("匯出完整備份（JSON）") },
            )
            ListItem(
                modifier = Modifier.clickable { restoreLauncher.launch(arrayOf("application/json")) },
                leadingContent = { Icon(Icons.Filled.Restore, null) },
                headlineContent = { Text("還原資料") },
                supportingContent = { Text("從備份還原（會覆蓋現有資料）") },
            )

            SectionHeader("關於")
            ListItem(headlineContent = { Text("記帳本") }, supportingContent = { Text("版本 1.0") })

            // Clears the translucent bottom bar the page scrolls behind.
            Spacer(Modifier.height(LocalBottomBarPadding.current))
        }
    }

    colorTarget?.let { target ->
        ColorPickerDialog(
            initial = currentColorArgb(theme, target),
            fallback = currentColor(theme, target).toArgb(),
            onConfirm = { viewModel.setColor(target, it); colorTarget = null },
            onReset = { viewModel.setColor(target, null); colorTarget = null },
            onDismiss = { colorTarget = null },
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 6.dp),
    )
}

@Composable
private fun DarkModeChip(label: String, mode: DarkMode, current: DarkMode, onSelect: (DarkMode) -> Unit) {
    FilterChip(selected = current == mode, onClick = { onSelect(mode) }, label = { Text(label) })
}

@Composable
private fun ColorRow(label: String, color: Color, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(label) },
        trailingContent = {
            Box(
                Modifier.size(28.dp).clip(CircleShape).background(color),
            )
        },
    )
}

private fun currentColorArgb(theme: ThemeState, target: ColorTarget): Int? = when (target) {
    ColorTarget.PRIMARY -> theme.primaryOverride
    ColorTarget.SECONDARY -> theme.secondaryOverride
    ColorTarget.BACKGROUND -> theme.backgroundOverride
    ColorTarget.INCOME -> theme.incomeOverride
    ColorTarget.EXPENSE -> theme.expenseOverride
}

private fun currentColor(theme: ThemeState, target: ColorTarget): Color {
    currentColorArgb(theme, target)?.let { return Color(it) }
    val preset = ThemePresets.byId(theme.presetId).light
    return when (target) {
        ColorTarget.PRIMARY -> preset.primary
        ColorTarget.SECONDARY -> preset.secondary
        ColorTarget.BACKGROUND -> preset.background
        ColorTarget.INCOME -> IncomeGreen
        ColorTarget.EXPENSE -> ExpenseRed
    }
}
