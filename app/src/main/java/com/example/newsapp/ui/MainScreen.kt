package com.example.newsapp.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import com.example.newsapp.R

import com.example.newsapp.ui.screens.AllArticlesScreen
import com.example.newsapp.ui.screens.ArticleDetailScreen
import com.example.newsapp.ui.screens.FavoritesScreen

private const val ROUTE_ALL = "all"
private const val ROUTE_FAVORITES = "favorites"
private const val ROUTE_DETAIL = "detail"

private fun detailRoute(id: Long): String = "$ROUTE_DETAIL/$id"

enum class Tab { ALL, FAVORITES }

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val isWide = rememberWideLayout()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = remember(navBackStackEntry) {
        navBackStackEntry?.destination?.route ?: ROUTE_ALL
    }
    val currentTab = remember(currentRoute) {
        if (currentRoute.startsWith(ROUTE_FAVORITES)) Tab.FAVORITES else Tab.ALL
    }

    var selectedArticleId by rememberSaveable { mutableStateOf<Long?>(null) }

    // --- ROTATION STATE HANDLER ---
    // Smoothly hands off the detail view between the NavHost (Narrow) and the Side Pane (Wide)
    LaunchedEffect(isWide) {
        if (isWide) {
            // Narrow -> Wide: If user was on the detail screen, pop it and open the side pane instead
            if (currentRoute.startsWith(ROUTE_DETAIL)) {
                val id = navBackStackEntry?.arguments?.getString("id")?.toLongOrNull()
                if (id != null) selectedArticleId = id
                navController.popBackStack()
            }
        } else {
            // Wide -> Narrow: If the side pane was open, navigate to the full detail screen and close the pane
            if (selectedArticleId != null) {
                navController.navigate(detailRoute(selectedArticleId!!)) { launchSingleTop = true }
                selectedArticleId = null
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Hide bottom bar if we are on a narrow screen looking at a detail page
            if (!isWide && currentRoute.startsWith(ROUTE_DETAIL)) return@Scaffold
            BottomBar(currentTab = currentTab, navController = navController)
        }
    ) { innerPadding ->

        // The Root Container for our responsive layout
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // --- LEFT PANE (Lists) ---
            // If the right pane is hidden, this weight(1f) takes up the WHOLE screen (Centered).
            // If the right pane appears, this gets pushed to the left to share the 50/50 space.
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                NavHost(
                    navController = navController,
                    startDestination = ROUTE_ALL,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(ROUTE_ALL) {
                        AllArticlesScreen(
                            onSelectArticle = { id ->
                                if (isWide) selectedArticleId = id
                                else navController.navigate(detailRoute(id))
                            },
                            snackbarHostState = snackbarHostState
                        )
                    }
                    composable(ROUTE_FAVORITES) {
                        FavoritesScreen(
                            onSelectArticle = { id ->
                                if (isWide) selectedArticleId = id
                                else navController.navigate(detailRoute(id))
                            },
                            snackbarHostState = snackbarHostState
                        )
                    }
                    composable(
                        route = "$ROUTE_DETAIL/{id}",
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) },
                        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) }
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                        if (id != null) {
                            ArticleDetailScreen(
                                articleId = id,
                                onBack = { navController.popBackStack() },
                                showBackButton = true
                            )
                        }
                    }
                }
            }

            // --- RIGHT PANE (Detail) ---
            // AnimatedVisibility handles the smooth slide-in, naturally pushing the left pane over.
            AnimatedVisibility(
                visible = isWide && selectedArticleId != null,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Divider(modifier = Modifier.width(1.dp).fillMaxHeight())
                    ArticleDetailScreen(
                        articleId = selectedArticleId ?: 0L,
                        // Closing the pane just requires nullifying the state
                        onBack = { selectedArticleId = null },
                        showBackButton = true
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBar(
    currentTab: Tab,
    navController: NavHostController
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentTab == Tab.ALL,
            onClick = { navController.navigate(ROUTE_ALL) { launchSingleTop = true } },
            icon = { Icon(painter = painterResource(R.drawable.all_icon), contentDescription = "All articles") },
            label = { Text("All") }
        )
        NavigationBarItem(
            selected = currentTab == Tab.FAVORITES,
            onClick = { navController.navigate(ROUTE_FAVORITES) { launchSingleTop = true } },
            icon = { Icon(painter = painterResource(R.drawable.pin_filled), contentDescription = "Favorites") },
            label = { Text("Favorites") }
        )
    }
}

@Composable
private fun rememberWideLayout(): Boolean {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    return screenWidthDp >= 840.dp
}


