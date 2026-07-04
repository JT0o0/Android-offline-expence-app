package com.toting.ledger.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toting.ledger.data.model.TxType
import com.toting.ledger.ui.components.CategoryAvatar
import com.toting.ledger.ui.components.CategoryIcons
import com.toting.ledger.ui.components.LocalHazeState
import com.toting.ledger.ui.components.animatedMoneyText
import com.toting.ledger.ui.components.glassBackdrop
import com.toting.ledger.ui.components.glassSource
import com.toting.ledger.ui.components.pressScale
import com.toting.ledger.ui.navigation.LocalBottomBarPadding
import com.toting.ledger.ui.theme.Ledger
import com.toting.ledger.util.DateUtils
import com.toting.ledger.util.MoneyFormatter

@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onEntryClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val bottomBarPadding = LocalBottomBarPadding.current
    // Shared blur state: this page's content is the backdrop for the glass month header
    // and the bottom bar.
    val hazeState = LocalHazeState.current
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeight = with(LocalDensity.current) { headerHeightPx.toDp() }

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().glassSource(hazeState)) {
            AnimatedContent(
                targetState = state.days.isEmpty(),
                transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(200)) },
                label = "homeContent",
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyHint(Modifier.fillMaxSize().padding(top = headerHeight))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = headerHeight + 4.dp,
                            bottom = 88.dp + bottomBarPadding,
                        ),
                    ) {
                        state.days.forEach { day ->
                            item(key = "header-${day.date}") {
                                DayHeaderRow(day, Modifier.animateItem())
                            }
                            items(day.items, key = { it.tx.id }) { row ->
                                TransactionRow(
                                    row = row,
                                    onClick = { onEntryClick(row.tx.id) },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                }
            }
        }

        MonthSummaryHeader(
            state = state,
            onPrev = viewModel::previousMonth,
            onNext = viewModel::nextMonth,
            onToday = viewModel::goToToday,
            modifier = Modifier
                .align(Alignment.TopCenter)
                // Measured height feeds the list's top padding — no hardcoded header size.
                .onSizeChanged { headerHeightPx = it.height }
                .glassBackdrop(hazeState),
        )

        val fabInteraction = remember { MutableInteractionSource() }
        AnimatedVisibility(
            visibleState = remember { MutableTransitionState(false).apply { targetState = true } },
            enter = scaleIn(
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(),
            // The page draws behind the glass bottom bar; lift the FAB above it.
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp + bottomBarPadding),
        ) {
            FloatingActionButton(
                onClick = onAddClick,
                interactionSource = fabInteraction,
                modifier = Modifier.pressScale(fabInteraction),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "記一筆")
            }
        }
    }
}

@Composable
private fun MonthSummaryHeader(
    state: HomeUiState,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The glass backdrop comes from the hazeEffect in [modifier]; statusBarsPadding sits
    // inside it so the blur extends edge-to-edge behind the status bar.
    Column(modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrev) { Icon(Icons.Filled.ChevronLeft, "上個月") }
            AnimatedContent(
                targetState = state.yearMonth,
                transitionSpec = {
                    val forward = targetState > initialState
                    (slideInHorizontally(tween(220)) { if (forward) it else -it } + fadeIn(tween(220))) togetherWith
                        (slideOutHorizontally(tween(220)) { if (forward) -it else it } + fadeOut(tween(220)))
                },
                label = "homeMonthTitle",
            ) { yearMonth ->
                Text(
                    text = DateUtils.formatMonth(yearMonth),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            IconButton(onClick = onNext) { Icon(Icons.Filled.ChevronRight, "下個月") }
            Box(Modifier.weight(1f))
            TextButton(onClick = onToday) { Text("今天") }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SummaryStat("收入", state.monthIncome, Ledger.colors.income)
            SummaryStat("支出", state.monthExpense, Ledger.colors.expense)
            SummaryStat(
                label = "結餘",
                amountMinor = state.monthNet,
                color = if (state.monthNet >= 0) Ledger.colors.income else Ledger.colors.expense,
            )
        }
    }
}

@Composable
private fun SummaryStat(label: String, amountMinor: Long, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(animatedMoneyText(amountMinor), style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DayHeaderRow(day: DayGroup, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = DateUtils.formatDayHeader(day.date),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(Modifier.weight(1f))
        if (day.income > 0) {
            Text(
                text = "+" + MoneyFormatter.format(day.income),
                style = MaterialTheme.typography.labelMedium,
                color = Ledger.colors.income,
            )
            Text("  ", style = MaterialTheme.typography.labelMedium)
        }
        if (day.expense > 0) {
            Text(
                text = "-" + MoneyFormatter.format(day.expense),
                style = MaterialTheme.typography.labelMedium,
                color = Ledger.colors.expense,
            )
        }
    }
}

@Composable
private fun TransactionRow(row: TxRow, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val tx = row.tx
    val isTransfer = tx.type == TxType.TRANSFER
    val avatarColor = when {
        isTransfer -> MaterialTheme.colorScheme.secondary
        row.category != null -> Color(row.category.colorArgb)
        else -> MaterialTheme.colorScheme.primary
    }
    val icon = if (isTransfer) Icons.Filled.SwapHoriz else CategoryIcons.iconFor(row.category?.iconKey)

    val title = when {
        isTransfer -> "轉帳"
        else -> row.category?.name ?: "未分類"
    }
    val subtitle = buildString {
        if (isTransfer) {
            append(row.account?.name ?: "?")
            append(" → ")
            append(row.transferTo?.name ?: "?")
        } else {
            append(row.account?.name ?: "")
            if (tx.note.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(tx.note)
            }
        }
    }

    val amountColor = when (tx.type) {
        TxType.INCOME -> Ledger.colors.income
        TxType.EXPENSE -> Ledger.colors.expense
        TxType.TRANSFER -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val amountText = when (tx.type) {
        TxType.INCOME -> "+" + MoneyFormatter.format(tx.amountMinor)
        TxType.EXPENSE -> "-" + MoneyFormatter.format(tx.amountMinor)
        TxType.TRANSFER -> MoneyFormatter.format(tx.amountMinor)
    }

    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = { CategoryAvatar(color = avatarColor, icon = icon) },
        headlineContent = { Text(title) },
        supportingContent = if (subtitle.isNotBlank()) {
            { Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else null,
        trailingContent = {
            Text(amountText, color = amountColor, fontWeight = FontWeight.SemiBold)
        },
    )
}

@Composable
private fun EmptyHint(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "這個月還沒有紀錄\n點右下角 + 開始記帳",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
