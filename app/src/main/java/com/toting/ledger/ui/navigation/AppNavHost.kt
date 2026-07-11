package com.toting.ledger.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.toting.ledger.ui.accounts.AccountsScreen
import com.toting.ledger.ui.components.AppBackground
import com.toting.ledger.ui.components.BOTTOM_BAR_GLASS_FACTOR
import com.toting.ledger.ui.components.LocalHazeState
import com.toting.ledger.ui.components.glassStyle
import com.toting.ledger.ui.entry.EntryScreen
import com.toting.ledger.ui.home.HomeScreen
import com.toting.ledger.ui.settings.SettingsScreen
import com.toting.ledger.ui.settings.category.CategoryListScreen
import com.toting.ledger.ui.stats.StatsScreen
import com.toting.ledger.ui.theme.LocalGlassAlpha
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private const val ANIM_MS = 320

/**
 * Space the translucent bottom bar occupies (bar + navigation-bar inset). Tab pages draw
 * behind the glass bar, so each pads its own scrollable content with this to keep the
 * last items reachable.
 */
val LocalBottomBarPadding = compositionLocalOf<Dp> { 0.dp }

/**
 * Root navigation. [MAIN_ROUTE] hosts the four swipeable tabs; the entry and category
 * screens are full-screen routes that animate in (slide up / slide from the side).
 */
@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    // Hoisted above the NavHost so the selected tab survives navigating to full-screen routes.
    val pagerState = rememberPagerState(pageCount = { TopDest.all.size })

    NavHost(
        navController = navController,
        startDestination = MAIN_ROUTE,
        modifier = modifier.fillMaxSize(),
    ) {
        composable(
            route = MAIN_ROUTE,
            // Full-screen routes (entry / categories) slide in OVER the main screen: keep
            // it fully opaque until the transition ends instead of the default fadeOut —
            // two translucent layers would expose the window background as a flash.
            exitTransition = { ExitTransition.KeepUntilTransitionsFinished },
            // Coming back it's already sitting beneath the popped screen; no animation.
            popEnterTransition = { EnterTransition.None },
        ) {
            MainTabs(
                pagerState = pagerState,
                onAddClick = { navController.navigate(EntryRoute.create()) },
                onEntryClick = { id -> navController.navigate(EntryRoute.create(id)) },
                onOpenCategories = { navController.navigate(SettingsRoutes.CATEGORIES) },
            )
        }

        composable(
            route = EntryRoute.PATTERN,
            arguments = listOf(
                navArgument(EntryRoute.ARG_TX_ID) { type = NavType.LongType; defaultValue = -1L },
            ),
            // Pure slide, no fade: the screen is opaque and covers the held main screen
            // like a bottom sheet.
            enterTransition = {
                slideInVertically(tween(ANIM_MS, easing = FastOutSlowInEasing)) { it }
            },
            popExitTransition = {
                slideOutVertically(tween(ANIM_MS, easing = FastOutSlowInEasing)) { it }
            },
        ) { backStackEntry ->
            val txId = backStackEntry.arguments?.getLong(EntryRoute.ARG_TX_ID) ?: -1L
            EntryScreen(txId = txId, onClose = { navController.popBackStack() })
        }

        composable(
            route = SettingsRoutes.CATEGORIES,
            enterTransition = { slideIntoContainer(SlideDirection.Left, tween(ANIM_MS, easing = FastOutSlowInEasing)) },
            popExitTransition = { slideOutOfContainer(SlideDirection.Right, tween(ANIM_MS, easing = FastOutSlowInEasing)) },
        ) {
            CategoryListScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun MainTabs(
    pagerState: PagerState,
    onAddClick: () -> Unit,
    onEntryClick: (Long) -> Unit,
    onOpenCategories: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    // One shared blur state: each page marks its scrolling content with glassSource;
    // the bottom bar and page headers sample it. The pager itself must NOT be a source —
    // a hazeEffect nested inside a hazeSource subtree silently draws nothing in haze 1.3.
    val hazeState = remember { HazeState() }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                LedgerBottomBar(
                    selectedIndex = pagerState.currentPage,
                    onSelect = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
                    modifier = Modifier.hazeEffect(
                        state = hazeState,
                        style = glassStyle(
                            alpha = LocalGlassAlpha.current * BOTTOM_BAR_GLASS_FACTOR,
                        ),
                    ),
                )
            },
        ) { innerPadding ->
            // Deliberately NOT padded by innerPadding: pages draw behind the glass bar and
            // pad their own scrollables with LocalBottomBarPadding. Top insets are handled
            // by each page's header/top bar.
            CompositionLocalProvider(
                LocalBottomBarPadding provides innerPadding.calculateBottomPadding(),
                LocalHazeState provides hazeState,
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1,
                ) { page ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            // Subtle depth while swiping: neighbours fade and shrink slightly.
                            .graphicsLayer {
                                val distance = pagerState
                                    .getOffsetDistanceInPages(page)
                                    .absoluteValue
                                    .coerceIn(0f, 1f)
                                alpha = 1f - 0.25f * distance
                                scaleX = 1f - 0.05f * distance
                                scaleY = scaleX
                            },
                    ) {
                        when (TopDest.all[page]) {
                            TopDest.Home -> HomeScreen(onAddClick = onAddClick, onEntryClick = onEntryClick)
                            TopDest.Stats -> StatsScreen()
                            TopDest.Accounts -> AccountsScreen()
                            TopDest.Settings -> SettingsScreen(onOpenCategories = onOpenCategories)
                        }
                    }
                }
            }
        }
    }
}
