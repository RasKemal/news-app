package com.example.newsapp.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
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

    // Wide layout selection (no navigation to detail route).
    val selectedIdState = remember { mutableStateOf<Long?>(null) }
    val selectedId by selectedIdState

    LaunchedEffect(currentRoute) {
        if (isWide) selectedIdState.value = null
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isWide && currentRoute.startsWith(ROUTE_DETAIL)) {
                // Hide bottom navigation on detail screen (narrow layouts).
            } else {
                BottomBar(
                    currentTab = currentTab,
                    navController = navController
                )
            }
        }
    ) { innerPadding ->
        if (isWide) {
            WideLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                navController = navController,
                selectedId = selectedId,
                onSelectArticle = { id -> selectedIdState.value = id },
                snackbarHostState = snackbarHostState
            )
        } else {
            NarrowLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                navController = navController,
                snackbarHostState = snackbarHostState
            )
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
            icon = {
                Icon(
                    painter = painterResource(R.drawable.all_icon),
                    contentDescription = "All articles"
                )
            },
            label = { Text("All") }
        )
        NavigationBarItem(
            selected = currentTab == Tab.FAVORITES,
            onClick = { navController.navigate(ROUTE_FAVORITES) { launchSingleTop = true } },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.pin_filled),
                    contentDescription = "Favorites"
                )
            },
            label = { Text("Favorites") }
        )
    }
}

@Composable
private fun WideLayout(
    modifier: Modifier,
    navController: NavHostController,
    selectedId: Long?,
    onSelectArticle: (Long) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Row(modifier = modifier) {
        Box(modifier = Modifier.weight(1f)) {
            NavHost(
                navController = navController,
                startDestination = ROUTE_ALL,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(ROUTE_ALL) {
                    AllArticlesScreen(
                        onSelectArticle = onSelectArticle,
                        snackbarHostState = snackbarHostState
                    )
                }
                composable(ROUTE_FAVORITES) {
                    FavoritesScreen(
                        onSelectArticle = onSelectArticle,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
        Divider(modifier = Modifier.width(1.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (selectedId != null) {
                ArticleDetailScreen(
                    articleId = selectedId,
                    onBack = {},
                    showBackButton = false
                )
            } else {
                EmptyDetailPane()
            }
        }
    }
}

@Composable
private fun NarrowLayout(
    modifier: Modifier,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    Column(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = ROUTE_ALL,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(ROUTE_ALL) {
                AllArticlesScreen(
                    onSelectArticle = { id -> navController.navigate(detailRoute(id)) },
                    snackbarHostState = snackbarHostState
                )
            }
            composable(ROUTE_FAVORITES) {
                FavoritesScreen(
                    onSelectArticle = { id -> navController.navigate(detailRoute(id)) },
                    snackbarHostState = snackbarHostState
                )
            }
            composable(
                route = "$ROUTE_DETAIL/{id}",
                // SLIDE IN: When opening an article, slide it in from the right edge
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(400) // 400ms for a smooth, readable slide
                    )
                },
                // SLIDE OUT: When hitting the back button, slide it back out to the right
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(400)
                    )
                }
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                if (id != null) {
                    ArticleDetailScreen(
                        articleId = id,
                        onBack = { navController.popBackStack() },
                        showBackButton = true
                    )
                } else {
                    EmptyDetailPane()
                }
            }
        }
    }
}

@Composable
private fun EmptyDetailPane() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Select an article")
    }
}

@Composable
private fun rememberWideLayout(): Boolean {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    return screenWidthDp >= 840.dp
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun MainScreenPreview() {
    com.example.newsapp.ui.theme.NewsAppTheme {
        MainScreen()
    }
}

