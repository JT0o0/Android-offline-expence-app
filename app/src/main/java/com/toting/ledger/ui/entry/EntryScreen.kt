package com.toting.ledger.ui.entry

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toting.ledger.data.local.CategoryEntity
import com.toting.ledger.data.model.TxType
import com.toting.ledger.ui.components.CategoryAvatar
import com.toting.ledger.ui.components.CategoryIcons
import com.toting.ledger.ui.components.contentColorOn
import com.toting.ledger.ui.components.pressScale
import com.toting.ledger.ui.theme.Ledger
import com.toting.ledger.util.MoneyFormatter
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    txId: Long,
    onClose: () -> Unit,
    viewModel: EntryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEdit) "編輯" else "記一筆") },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "關閉") }
                },
                actions = {
                    if (state.isEdit) {
                        IconButton(onClick = { viewModel.delete(onClose) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "刪除")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding(),
        ) {
            // 上半部可捲動：大字體/大顯示比例把空間吃掉時，類別仍捲得到、點得到；
            // 鍵盤與底部動作鈕固定釘在下方。
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                AmountDisplay(state = state, onClear = viewModel::clear)
                CategoryGrid(
                    categories = state.categories,
                    selectedId = state.selectedCategoryId,
                    onSelect = viewModel::selectCategory,
                )
                MetaSection(
                    state = state,
                    onAccount = viewModel::selectAccount,
                    onDate = viewModel::setDate,
                    onNote = viewModel::setNote,
                )
            }
            CalculatorKeypad(
                onDigit = viewModel::inputDigit,
                onDot = viewModel::inputDot,
                onOperator = viewModel::inputOperator,
                onBackspace = viewModel::backspace,
                onClearAll = viewModel::clear,
            )
            BottomActionBar(
                state = state,
                onSelectType = viewModel::setType,
                onSave = { viewModel.save(onClose) },
            )
        }
    }
}

/**
 * 底部動作列：金額為 0 時顯示「支出/收入」切換，輸入金額後變形為「完成」鈕
 * （退格清到 0 就變回切換）。容器固定 50dp 高，變形時鍵盤不會跳動。
 */
@Composable
private fun BottomActionBar(
    state: EntryUiState,
    onSelectType: (TxType) -> Unit,
    onSave: () -> Unit,
) {
    AnimatedContent(
        targetState = state.showTypeToggle,
        transitionSpec = {
            (fadeIn(tween(180)) + scaleIn(initialScale = 0.92f, animationSpec = tween(180)))
                .togetherWith(fadeOut(tween(120)) + scaleOut(targetScale = 0.92f, animationSpec = tween(120)))
        },
        label = "entryBottomAction",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .height(50.dp),
    ) { showToggle ->
        if (showToggle) {
            Row(
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ToggleChip(
                    "支出", state.type == TxType.EXPENSE, Ledger.colors.expense,
                    Modifier.weight(1f).fillMaxHeight(),
                ) { onSelectType(TxType.EXPENSE) }
                ToggleChip(
                    "收入", state.type == TxType.INCOME, Ledger.colors.income,
                    Modifier.weight(1f).fillMaxHeight(),
                ) { onSelectType(TxType.INCOME) }
            }
        } else {
            val color = if (state.type == TxType.EXPENSE) Ledger.colors.expense else Ledger.colors.income
            Button(
                onClick = onSave,
                enabled = state.canSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = color,
                    contentColor = contentColorOn(color),
                ),
                modifier = Modifier.fillMaxSize(),
            ) {
                Text("完成", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun ToggleChip(text: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    val container by animateColorAsState(
        targetValue = if (selected) color else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(200),
        label = "toggleContainer",
    )
    val content by animateColorAsState(
        targetValue = if (selected) contentColorOn(color) else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "toggleContent",
    )
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.pressScale(interactionSource),
        shape = CircleShape,
        color = container,
        contentColor = content,
        interactionSource = interactionSource,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
private fun AmountDisplay(state: EntryUiState, onClear: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
    ) {
        Text(
            text = state.expression.ifEmpty { "0" },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (state.expression.isNotEmpty()) {
                TextButton(onClick = onClear) { Text("清除") }
            }
            Spacer(Modifier.weight(1f))
            val sign = if (state.type == TxType.EXPENSE) "-" else "+"
            Text(
                text = sign + MoneyFormatter.format(state.amountMinor),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (state.type == TxType.EXPENSE) Ledger.colors.expense else Ledger.colors.income,
            )
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<CategoryEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 非-lazy 排版：類別頂多十餘個，且 Lazy 網格不能放進 verticalScroll 區域。
    Column(modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
        categories.chunked(CATEGORY_COLUMNS).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { cat ->
                    CategoryCell(
                        category = cat,
                        selected = cat.id == selectedId,
                        onSelect = onSelect,
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(CATEGORY_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun CategoryCell(
    category: CategoryEntity,
    selected: Boolean,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = Color(category.colorArgb)
    val ringWidth by animateDpAsState(
        targetValue = if (selected) 2.dp else 0.dp,
        animationSpec = tween(180),
        label = "categoryRingWidth",
    )
    val ringColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(180),
        label = "categoryRingColor",
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(180),
        label = "categoryLabelColor",
    )
    Column(
        modifier.padding(6.dp).clickable { onSelect(category.id) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .border(ringWidth, ringColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CategoryAvatar(color = color, icon = CategoryIcons.iconFor(category.iconKey), size = 44.dp)
        }
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            color = labelColor,
        )
    }
}

@Composable
private fun MetaSection(
    state: EntryUiState,
    onAccount: (Long) -> Unit,
    onDate: (LocalDate) -> Unit,
    onNote: (String) -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AccountChip(state = state, onAccount = onAccount)
            DateChip(date = state.date, onDate = onDate)
        }
        OutlinedTextField(
            value = state.note,
            onValueChange = onNote,
            placeholder = { Text("備註") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AccountChip(state: EntryUiState, onAccount: (Long) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selected = state.accounts.firstOrNull { it.id == state.selectedAccountId }
    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(selected?.name ?: "帳戶") },
            leadingIcon = {
                Icon(Icons.Filled.AccountBalanceWallet, null, Modifier.size(AssistChipDefaults.IconSize))
            },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            state.accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.name) },
                    onClick = { onAccount(account.id); expanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateChip(date: LocalDate, onDate: (LocalDate) -> Unit) {
    var show by remember { mutableStateOf(false) }
    AssistChip(
        onClick = { show = true },
        label = { Text("${date.monthValue}/${date.dayOfMonth}") },
        leadingIcon = { Icon(Icons.Filled.CalendarMonth, null, Modifier.size(AssistChipDefaults.IconSize)) },
    )
    if (show) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.toEpochDay() * MILLIS_PER_DAY,
        )
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms ->
                        onDate(LocalDate.ofEpochDay(Math.floorDiv(ms, MILLIS_PER_DAY)))
                    }
                    show = false
                }) { Text("確定") }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text("取消") } },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private const val MILLIS_PER_DAY = 86_400_000L
private const val CATEGORY_COLUMNS = 4
