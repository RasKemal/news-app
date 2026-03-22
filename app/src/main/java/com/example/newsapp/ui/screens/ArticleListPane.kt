package com.example.newsapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.shadow
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.compose.LazyPagingItems
import com.example.newsapp.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import com.example.newsapp.domain.model.Article
import com.example.newsapp.domain.model.ArticleListLayout
import com.example.newsapp.domain.model.UserPreferences
import com.example.newsapp.ui.Tab
import com.example.newsapp.ui.components.ArticleGridItem
import com.example.newsapp.ui.components.ArticleGridPlaceholder
import com.example.newsapp.ui.components.ArticleListItem
import com.example.newsapp.ui.components.ArticleListPlaceholder

data class ArticleListActions(
    val onSearchQueryChanged: (String) -> Unit,
    val onSelectArticle: (Long) -> Unit,
    val onToggleFavorite: (Article) -> Unit,
    val onRequestLayout: (ArticleListLayout) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListPane(
    tab: Tab,
    showSearch: Boolean,
    userPreferences: UserPreferences,
    searchQuery: String,
    articles: Flow<PagingData<Article>>,
    actions: ArticleListActions
) {
    val items = articles.collectAsLazyPagingItems()

    val refreshState = items.loadState.refresh
    val appendState = items.loadState.append
    val isListLayout = userPreferences.articleListLayout == ArticleListLayout.LIST
    val showAppendLoading = appendState is LoadState.Loading

    // We derive the refreshing state directly from Paging 3's engine!
    val isRefreshing = refreshState is LoadState.Loading && items.itemCount > 0

    // Hoisted scroll states
    val listState = rememberLazyListState()
    val gridState = rememberLazyStaggeredGridState()

    Column(modifier = Modifier.fillMaxSize()) {

        // --- 1. HEADER ---
        ArticleListHeader(
            tab = tab,
            showSearch = showSearch,
            searchQuery = searchQuery,
            isListLayout = isListLayout,
            onSearchQueryChanged = actions.onSearchQueryChanged,
            onRequestLayout = actions.onRequestLayout
        )

        // --- 2. CONDITIONALLY REFRESHABLE BODY ---
        val bodyModifier = Modifier.weight(1f) // Fills the remaining vertical space

        if (tab == Tab.ALL) {
            // NEW 1.3.0 API: Automatically handles the physics, indicators, and state
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { items.refresh() }, // Triggers RemoteMediator LoadType.REFRESH
                modifier = bodyModifier
            ) {
                ArticleListContent(
                    items = items, isListLayout = isListLayout, listState = listState,
                    gridState = gridState, showAppendLoading = showAppendLoading,
                    appendState = appendState, refreshState = refreshState,
                    tab = tab, searchQuery = searchQuery,
                    onSelectArticle = actions.onSelectArticle,
                    onToggleFavorite = actions.onToggleFavorite
                )
            }
        } else {
            // Favorites tab gets a standard Box (No pull-to-refresh physics)
            Box(modifier = bodyModifier) {
                ArticleListContent(
                    items = items, isListLayout = isListLayout, listState = listState,
                    gridState = gridState, showAppendLoading = showAppendLoading,
                    appendState = appendState, refreshState = refreshState,
                    tab = tab, searchQuery = searchQuery,
                    onSelectArticle = actions.onSelectArticle,       // <-- Unwrapped here
                    onToggleFavorite = actions.onToggleFavorite
                )
            }
        }
    }
}

// ============================================================================
// EXTRACTED LIST CONTENT
// ============================================================================

@Composable
private fun ArticleListContent(
    items: LazyPagingItems<Article>,
    isListLayout: Boolean,
    listState: LazyListState,
    gridState: LazyStaggeredGridState,
    showAppendLoading: Boolean,
    appendState: LoadState,
    refreshState: LoadState,
    tab: Tab,
    searchQuery: String,
    onSelectArticle: (Long) -> Unit,
    onToggleFavorite: (Article) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = isListLayout,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "listGridTransition",
                modifier = Modifier.weight(1f)
            ) { targetIsList ->
                if (targetIsList) {
                    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                        items(
                            count = items.itemCount,
                            key = { index -> items[index]?.id ?: index },
                            contentType = { "article" }
                        ) { index ->
                            val article = items[index]
                            if (article != null) {
                                ArticleListItem(
                                    article = article,
                                    onClick = { onSelectArticle(article.id) },
                                    onToggleFavorite = { onToggleFavorite(article) }
                                )
                            } else {
                                ArticleListPlaceholder()
                            }
                        }
                        if (showAppendLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) { CircularProgressIndicator() }
                            }
                        }
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        state = gridState,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            count = items.itemCount,
                            key = { index -> items[index]?.id ?: index },
                            contentType = { "article" }
                        ) { index ->
                            val article = items[index]
                            if (article != null) {
                                ArticleGridItem(
                                    article = article,
                                    onClick = { onSelectArticle(article.id) },
                                    onToggleFavorite = { onToggleFavorite(article) }
                                )
                            } else {
                                ArticleGridPlaceholder()
                            }
                        }
                        if (showAppendLoading) {
                            item(key = "append-loading") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) { CircularProgressIndicator() }
                            }
                        }
                    }
                }
            }

            if (appendState is LoadState.Error) {
                Text(
                    text = appendState.error.message ?: "Failed to load more",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // --- OVERLAYS & EMPTY STATES ---
        val isEmpty = items.itemCount == 0
        val isTrulyEmpty = items.loadState.source.refresh is LoadState.NotLoading &&
                items.loadState.source.append.endOfPaginationReached &&
                (items.loadState.mediator?.refresh is LoadState.NotLoading || items.loadState.mediator == null)
        val hasError =
            refreshState is LoadState.Error || items.loadState.mediator?.refresh is LoadState.Error

        if (isEmpty) {
            EmptyStateOverlay(
                hasError = hasError,
                isTrulyEmpty = isTrulyEmpty,
                refreshState = refreshState,
                items = items,
                tab = tab,
                query = searchQuery
            )
        } else {
            // Error Pill for background refresh failures
            if (refreshState is LoadState.Error) {
                val errorMsg = refreshState.error.message ?: "Error updating"
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ============================================================================
// EXTRACTED SUB-COMPONENTS
// ============================================================================

@Composable
private fun ArticleListHeader(
    tab: Tab,
    showSearch: Boolean,
    searchQuery: String,
    isListLayout: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onRequestLayout: (ArticleListLayout) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (tab == Tab.ALL) "All Articles" else "Favorite Articles",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            val shape = RoundedCornerShape(999.dp)
            val bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            val glow = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)

            Row(
                modifier = Modifier
                    .shadow(elevation = 10.dp, shape = shape, ambientColor = glow, spotColor = glow)
                    .background(bg, shape)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            color = if (isListLayout) MaterialTheme.colorScheme.surface else bg,
                            shape = shape
                        )
                        .clickable { if (!isListLayout) onRequestLayout(ArticleListLayout.LIST) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.list_icon),
                        contentDescription = "List layout",
                        tint = if (isListLayout) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.45f
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .background(
                            color = if (!isListLayout) MaterialTheme.colorScheme.surface else bg,
                            shape = shape
                        )
                        .clickable { if (isListLayout) onRequestLayout(ArticleListLayout.GRID) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.gird_icon),
                        contentDescription = "Grid layout",
                        tint = if (!isListLayout) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.45f
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (showSearch) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                placeholder = { Text("Search articles…") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(999.dp)
            )
        }
    }
}

@Composable
private fun EmptyStateOverlay(
    hasError: Boolean,
    isTrulyEmpty: Boolean,
    refreshState: LoadState,
    items: LazyPagingItems<Article>,
    tab: Tab,
    query: String
) {
    val errorMessage = (refreshState as? LoadState.Error)?.error?.message
        ?: (items.loadState.mediator?.refresh as? LoadState.Error)?.error?.message
        ?: "Failed to load articles"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        when {
            hasError -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = errorMessage, style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { items.retry() }, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Retry")
                    }
                }
            }

            isTrulyEmpty -> {
                val trimmedQuery = query.trim()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    if (tab == Tab.FAVORITES) {
                        Icon(
                            painter = painterResource(id = R.drawable.favorites_nodata_icon),
                            contentDescription = "No favorites",
                            modifier = Modifier.size(120.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No favorites yet.",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Articles you pin will appear here so you can easily read them later.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    } else if (trimmedQuery.isNotBlank()) {
                        Icon(
                            painter = painterResource(id = R.drawable.search_nodata_icon),
                            contentDescription = "No search results",
                            modifier = Modifier.size(120.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No results found.",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "We couldn't find any articles matching \"$trimmedQuery\".\nTry a different keyword.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.all_nodata_icon),
                            contentDescription = "No articles",
                            modifier = Modifier.size(120.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No articles yet.",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Could not load any articles, please check your network connection.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            else -> {
                SpaceLoadingIndicator(message = "Loading articles…")
            }
        }
    }
}

@Composable
private fun SpaceLoadingIndicator(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}