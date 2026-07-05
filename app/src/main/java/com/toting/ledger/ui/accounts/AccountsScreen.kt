package com.toting.ledger.ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toting.ledger.data.local.AccountEntity
import com.toting.ledger.data.model.AccountType
import com.toting.ledger.ui.components.CategoryAvatar
import com.toting.ledger.ui.components.CategoryIcons
import com.toting.ledger.ui.components.ColorSwatchPicker
import com.toting.ledger.ui.components.GlassSurface
import com.toting.ledger.ui.components.IconPicker
import com.toting.ledger.ui.components.LocalHazeState
import com.toting.ledger.ui.components.animatedMoneyText
import com.toting.ledger.ui.components.glassSource
import com.toting.ledger.ui.components.Palette
import com.toting.ledger.ui.components.colorpicker.ColorPickerDialog
import com.toting.ledger.ui.navigation.LocalBottomBarPadding
import com.toting.ledger.ui.theme.Ledger
import com.toting.ledger.util.MoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<AccountEntity?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("帳戶") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = {
                        editing = AccountEntity(
                            name = "",
                            type = AccountType.CASH,
                            iconKey = "wallet",
                            colorArgb = Palette.swatches.first(),
                        )
                    }) { Icon(Icons.Filled.Add, "新增帳戶") }
                },
            )
        },
    ) { padding ->
        // This page's content is the blur backdrop for the glass bottom bar.
        Column(Modifier.fillMaxSize().padding(padding).glassSource(LocalHazeState.current)) {
            TotalBalanceCard(total = items.sumOf { it.balanceMinor })
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = LocalBottomBarPadding.current),
            ) {
                items(items, key = { it.account.id }) { row ->
                    AccountRow(row, Modifier.animateItem()) { editing = row.account }
                }
            }
        }
    }

    editing?.let { account ->
        AccountEditDialog(
            initial = account,
            onDismiss = { editing = null },
            onSave = { viewModel.save(it); editing = null },
            onDelete = if (account.id != 0L) { { viewModel.delete(account); editing = null } } else null,
        )
    }
}

@Composable
private fun TotalBalanceCard(total: Long) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("總結餘", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = animatedMoneyText(total),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (total >= 0) Ledger.colors.income else Ledger.colors.expense,
            )
        }
    }
}

@Composable
private fun AccountRow(row: AccountWithBalance, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val account = row.account
    val color = account.colorArgb?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = { CategoryAvatar(color = color, icon = CategoryIcons.iconFor(account.iconKey)) },
        headlineContent = { Text(account.name) },
        supportingContent = { Text(accountTypeLabel(account.type), color = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = {
            Text(
                MoneyFormatter.format(row.balanceMinor),
                fontWeight = FontWeight.SemiBold,
                color = if (row.balanceMinor >= 0) MaterialTheme.colorScheme.onSurface else Ledger.colors.expense,
            )
        },
    )
}

private fun accountTypeLabel(type: AccountType): String = when (type) {
    AccountType.CASH -> "現金"
    AccountType.BANK -> "銀行"
    AccountType.CREDIT -> "信用卡"
}

@Composable
private fun AccountEditDialog(
    initial: AccountEntity,
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by remember { mutableStateOf(initial.name) }
    var type by remember { mutableStateOf(initial.type) }
    var iconKey by remember { mutableStateOf(initial.iconKey) }
    var colorArgb by remember { mutableStateOf(initial.colorArgb ?: Palette.swatches.first()) }
    var showCustomColor by remember { mutableStateOf(false) }
    var balanceText by remember {
        mutableStateOf(
            if (initial.id == 0L) "" else MoneyFormatter.toMajor(initial.initialBalanceMinor).stripTrailingZeros().toPlainString()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == 0L) "新增帳戶" else "編輯帳戶") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名稱") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("初始餘額") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(type == AccountType.CASH, { type = AccountType.CASH }, { Text("現金") })
                    FilterChip(type == AccountType.BANK, { type = AccountType.BANK }, { Text("銀行") })
                    FilterChip(type == AccountType.CREDIT, { type = AccountType.CREDIT }, { Text("信用卡") })
                }
                Text("圖示", style = MaterialTheme.typography.labelLarge)
                IconPicker(selectedKey = iconKey, onSelect = { iconKey = it }, modifier = Modifier.height(132.dp))
                Text("顏色", style = MaterialTheme.typography.labelLarge)
                ColorSwatchPicker(selected = colorArgb, onSelect = { colorArgb = it }, modifier = Modifier.height(112.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(28.dp).clip(CircleShape).background(Color(colorArgb)))
                    TextButton(onClick = { showCustomColor = true }) { Text("自訂顏色…") }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    val balanceMinor = balanceText.trim().toBigDecimalOrNull()
                        ?.let { MoneyFormatter.toMinor(it) } ?: 0L
                    onSave(
                        initial.copy(
                            name = name.trim(),
                            type = type,
                            iconKey = iconKey,
                            colorArgb = colorArgb,
                            initialBalanceMinor = balanceMinor,
                        )
                    )
                },
            ) { Text("儲存") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, null)
                        Text("刪除")
                    }
                }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        },
    )

    if (showCustomColor) {
        ColorPickerDialog(
            initial = colorArgb,
            fallback = colorArgb,
            title = "自訂顏色",
            onConfirm = { colorArgb = it; showCustomColor = false },
            onDismiss = { showCustomColor = false },
        )
    }
}
