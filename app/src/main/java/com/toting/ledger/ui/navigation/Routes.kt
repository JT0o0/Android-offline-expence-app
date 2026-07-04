package com.toting.ledger.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** Top-level destinations shown in the bottom navigation bar. */
sealed class TopDest(val route: String, val label: String, val icon: ImageVector) {
    data object Home : TopDest("home", "首頁", Icons.Filled.MenuBook)
    data object Stats : TopDest("stats", "統計", Icons.Filled.PieChart)
    data object Accounts : TopDest("accounts", "帳戶", Icons.Filled.AccountBalanceWallet)
    data object Settings : TopDest("settings", "設定", Icons.Filled.Settings)

    companion object {
        val all = listOf(Home, Stats, Accounts, Settings)
    }
}

/** The tabbed home graph (hosts the swipeable bottom-nav pager). */
const val MAIN_ROUTE = "main"

/** Full-screen "add / edit a transaction" route. txId = -1 means add. */
object EntryRoute {
    const val ARG_TX_ID = "txId"
    const val PATTERN = "entry?txId={txId}"
    fun create(txId: Long = -1L): String = "entry?txId=$txId"
}

/** Sub-screens reached from the Settings tab. */
object SettingsRoutes {
    const val CATEGORIES = "settings/categories"
}
