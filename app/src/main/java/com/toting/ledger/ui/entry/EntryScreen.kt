package com.toting.ledger.ui.entry

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
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
            TypeToggle(type = state.type, onSelect = viewModel::setType)
            AmountDisplay(state = state, onClear = viewModel::clear)
            CategoryGrid(
                categories = state.categories,
                selectedId = state.selectedCategoryId,
                onSelect = viewModel::selectCategory,
                modifier = Modifier.weight(1f),
            )
            MetaSection(
                state = state,
                onAccount = viewModel::selectAccount,
                onDate = viewModel::setDate,
                onNote = viewModel::setNote,
            )
            CalculatorKeypad(
                onDigit = viewModel::inputDigit,
                onDot = viewModel::inputDot,
                onOperator = viewModel::inputOperator,
                onBackspace = viewModel::backspace,
                onClearAll = viewModel::clear,
            )
            Button(
                onClick = { viewModel.save(onClose) },
                enabled = state.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .height(50.dp),
            ) {
                Text("完成", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun TypeToggle(type: TxType, onSelect: (TxType) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ToggleChip("支出", type == TxType.EXPENSE, Ledger.colors.expense, Modifier.weight(1f)) { onSelect(TxType.EXPENSE) }
        ToggleChip("收入", type == TxType.INCOME, Ledger.colors.income, Modifier.weight(1f)) { onSelect(TxType.INCOME) }
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
        modifier = modifier.height(42.dp).pressScale(interactionSource),
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
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    ) {
        items(categories, key = { it.id }) { cat ->
            val selected = cat.id == selectedId
            val color = Color(cat.colorArgb)
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
                Modifier.padding(6.dp).clickable { onSelect(cat.id) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .border(ringWidth, ringColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    CategoryAvatar(color = color, icon = CategoryIcons.iconFor(cat.iconKey), size = 44.dp)
                }
                Text(
                    text = cat.name,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    color = labelColor,
                )
            }
        }
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
