package com.toting.ledger.ui.stats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toting.ledger.data.model.TxType
import com.toting.ledger.ui.components.GlassSurface
import com.toting.ledger.ui.components.LocalHazeState
import com.toting.ledger.ui.components.animatedMoneyText
import com.toting.ledger.ui.components.glassBackdrop
import com.toting.ledger.ui.components.glassSource
import com.toting.ledger.ui.navigation.LocalBottomBarPadding
import com.toting.ledger.ui.theme.Ledger
import com.toting.ledger.util.DateUtils
import com.toting.ledger.util.MoneyFormatter
import kotlin.math.roundToInt

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Shared blur state: this page's content is the backdrop for the glass month header
    // and the bottom bar.
    val hazeState = LocalHazeState.current
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeight = with(LocalDensity.current) { headerHeightPx.toDp() }

    // Charts sweep/grow in whenever the data changes (month switch, type toggle).
    val chartProgress = remember { Animatable(0f) }
    LaunchedEffect(state.slices, state.dailies) {
        chartProgress.snapTo(0f)
        chartProgress.animateTo(1f, tween(durationMillis = 700, easing = FastOutSlowInEasing))
    }

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().glassSource(hazeState)) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(headerHeight))

                // Summary
                GlassSurface {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Stat("收入", state.income, Ledger.colors.income)
                        Stat("支出", state.expense, Ledger.colors.expense)
                        Stat(
                            "結餘",
                            state.income - state.expense,
                            if (state.income - state.expense >= 0) Ledger.colors.income else Ledger.colors.expense,
                        )
                    }
                }

                // Type toggle for the breakdown
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(state.type == TxType.EXPENSE, { viewModel.setType(TxType.EXPENSE) }, { Text("支出") })
                    FilterChip(state.type == TxType.INCOME, { viewModel.setType(TxType.INCOME) }, { Text("收入") })
                }

                // Donut + legend
                Text("分類占比", style = MaterialTheme.typography.titleMedium)
                GlassSurface {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                                DonutChart(state.slices, Modifier.size(180.dp), progress = chartProgress.value)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        if (state.type == TxType.EXPENSE) "支出" else "收入",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        MoneyFormatter.format(state.shownTotal),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }

                        if (state.slices.isEmpty()) {
                            Text("這個月沒有資料", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.slices.forEach { slice ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(12.dp).clip(CircleShape).background(Color(slice.colorArgb)))
                                        Text(
                                            slice.name,
                                            modifier = Modifier.padding(start = 8.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                        Text(
                                            "${(slice.fraction * 100).roundToInt()}%",
                                            modifier = Modifier.padding(start = 6.dp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Box(Modifier.weight(1f))
                                        Text(MoneyFormatter.format(slice.amount), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                // Trend
                Text("每日趨勢", style = MaterialTheme.typography.titleMedium)
                GlassSurface {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TrendBars(
                            dailies = state.dailies,
                            incomeColor = Ledger.colors.income,
                            expenseColor = Ledger.colors.expense,
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            progress = chartProgress.value,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            LegendDot("收入", Ledger.colors.income)
                            LegendDot("支出", Ledger.colors.expense)
                        }
                    }
                }

                // Clears the translucent bottom bar the page scrolls behind.
                Spacer(Modifier.height(LocalBottomBarPadding.current))
            }
        }

        // Glass month header overlay; the blur extends behind the status bar.
        Row(
            Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { headerHeightPx = it.height }
                .glassBackdrop(hazeState)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = viewModel::previousMonth) { Icon(Icons.Filled.ChevronLeft, "上個月") }
            AnimatedContent(
                targetState = state.yearMonth,
                transitionSpec = {
                    val forward = targetState > initialState
                    (slideInHorizontally(tween(220)) { if (forward) it else -it } + fadeIn(tween(220))) togetherWith
                        (slideOutHorizontally(tween(220)) { if (forward) -it else it } + fadeOut(tween(220)))
                },
                label = "statsMonthTitle",
            ) { yearMonth ->
                Text(
                    DateUtils.formatMonth(yearMonth),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            IconButton(onClick = viewModel::nextMonth) { Icon(Icons.Filled.ChevronRight, "下個月") }
        }
    }
}

@Composable
private fun Stat(label: String, amountMinor: Long, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(animatedMoneyText(amountMinor), style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(label, modifier = Modifier.padding(start = 6.dp), style = MaterialTheme.typography.bodySmall)
    }
}
