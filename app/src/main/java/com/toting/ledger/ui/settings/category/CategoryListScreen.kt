package com.toting.ledger.ui.settings.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toting.ledger.data.local.CategoryEntity
import com.toting.ledger.data.model.TxType
import com.toting.ledger.ui.components.CategoryAvatar
import com.toting.ledger.ui.components.CategoryIcons
import com.toting.ledger.ui.components.ColorSwatchPicker
import com.toting.ledger.ui.components.IconPicker
import com.toting.ledger.ui.components.Palette
import com.toting.ledger.ui.components.colorpicker.ColorPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    onBack: () -> Unit,
    viewModel: CategoryListViewModel = hiltViewModel(),
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分類管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                },
                actions = {
                    IconButton(onClick = {
                        editing = CategoryEntity(
                            name = "",
                            type = TxType.EXPENSE,
                            iconKey = "category",
                            colorArgb = Palette.swatches.first(),
                        )
                    }) { Icon(Icons.Filled.Add, "新增分類") }
                },
            )
        },
    ) { padding ->
        val expense = categories.filter { it.type == TxType.EXPENSE }
        val income = categories.filter { it.type == TxType.INCOME }
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item { SectionHeader("支出分類") }
            items(expense, key = { it.id }) { CategoryRow(it, Modifier.animateItem()) { editing = it } }
            item { SectionHeader("收入分類") }
            items(income, key = { it.id }) { CategoryRow(it, Modifier.animateItem()) { editing = it } }
        }
    }

    editing?.let { category ->
        CategoryEditDialog(
            initial = category,
            onDismiss = { editing = null },
            onSave = { viewModel.save(it); editing = null },
            onDelete = if (category.id != 0L) {
                { viewModel.delete(category); editing = null }
            } else null,
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
private fun CategoryRow(category: CategoryEntity, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        leadingContent = {
            CategoryAvatar(color = Color(category.colorArgb), icon = CategoryIcons.iconFor(category.iconKey))
        },
        headlineContent = { Text(category.name) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryEditDialog(
    initial: CategoryEntity,
    onDismiss: () -> Unit,
    onSave: (CategoryEntity) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by remember { mutableStateOf(initial.name) }
    var type by remember { mutableStateOf(initial.type) }
    var iconKey by remember { mutableStateOf(initial.iconKey) }
    var colorArgb by remember { mutableStateOf(initial.colorArgb) }
    var showCustomColor by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == 0L) "新增分類" else "編輯分類") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名稱") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == TxType.EXPENSE, onClick = { type = TxType.EXPENSE }, label = { Text("支出") })
                    FilterChip(selected = type == TxType.INCOME, onClick = { type = TxType.INCOME }, label = { Text("收入") })
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
                onClick = { onSave(initial.copy(name = name.trim(), type = type, iconKey = iconKey, colorArgb = colorArgb)) },
            ) { Text("儲存") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, null)
                        Text("刪除", fontWeight = FontWeight.Normal)
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
