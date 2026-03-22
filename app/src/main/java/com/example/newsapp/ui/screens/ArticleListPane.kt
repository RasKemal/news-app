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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.LazyPagingItems
import com.example.newsapp.R
import kotlinx.coroutines.flow.Flow
import com.example.newsapp.domain.model.Article
import com.example.newsapp.domain.model.ArticleListLayout
import com.example.newsapp.domain.model.UserPreferences
import com.example.newsapp.ui.Tab
import com.example.newsapp.ui.components.ArticleGridItem
import com.example.newsapp.ui.components.ArticleGridPlaceholder
import com.example.newsapp.ui.components.ArticleListItem
import com.example.newsapp.ui.components.ArticleListPlaceholder
import com.example.newsapp.ui.components.GenericEmptyStateLayout
import com.example.newsapp.ui.components.PaginationErrorIndicator
import com.example.newsapp.ui.helper.ErrorText
import com.example.newsapp.ui.helper.toUserFriendlyText

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
    // Hoisted scroll states
    val listState = rememberLazyListState()
    val gridState = rememberLazyStaggeredGridState()

    val refreshState = items.loadState.refresh
    val isListLayout = userPreferences.articleListLayout == ArticleListLayout.LIST
    // We derive the refreshing state directly from Paging 3's engine!
    val isRefreshing = refreshState is LoadState.Loading && items.itemCount > 0

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
                    gridState = gridState, refreshState = refreshState,
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
                    gridState = gridState, refreshState = refreshState,
                    tab = tab, searchQuery = searchQuery,
                    onSelectArticle = actions.onSelectArticle,
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
    refreshState: LoadState,
    tab: Tab,
    searchQuery: String,
    onSelectArticle: (Long) -> Unit,
    onToggleFavorite: (Article) -> Unit
) {
    val showAppendLoading = items.loadState.mediator?.append is LoadState.Loading ||
            items.loadState.source.append is LoadState.Loading

    val appendError = (items.loadState.mediator?.append as? LoadState.Error)
        ?: (items.loadState.source.append as? LoadState.Error)

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

                        if (appendError != null) {
                            item(key = "append-error") {
                                PaginationErrorIndicator(
                                    error = appendError.error,
                                    onRetry = { items.retry() }
                                )
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
                            item(key = "append-loading", span = StaggeredGridItemSpan.FullLine) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) { CircularProgressIndicator() }
                            }
                        }

                        // 3. Add the Error Item to the bottom of the Grid!
                        if (appendError != null) {
                            item(key = "append-error", span = StaggeredGridItemSpan.FullLine) {
                                PaginationErrorIndicator(
                                    error = appendError.error,
                                    onRetry = { items.retry() }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- OVERLAYS & EMPTY STATES ---
        val isEmpty = items.itemCount == 0
        val isTrulyEmpty = if (items.loadState.mediator != null) {
            // For the ALL tab (Network + DB)
            val mediatorRefresh = items.loadState.mediator?.refresh
            mediatorRefresh is LoadState.NotLoading && mediatorRefresh.endOfPaginationReached
        } else {
            // For the FAVORITES tab (DB only)
            items.loadState.source.refresh is LoadState.NotLoading && items.loadState.source.append.endOfPaginationReached
        }
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
                val errorText = refreshState.error.toUserFriendlyText()

                Text(
                    text = stringResource(id = errorText.titleRes),
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        when {
            hasError -> {
                val error = (refreshState as? LoadState.Error)?.error
                    ?: (items.loadState.mediator?.refresh as? LoadState.Error)?.error

                val errorText = error?.toUserFriendlyText() ?: ErrorText(
                    titleRes = R.string.error_generic_title,
                    descriptionRes = R.string.error_generic_description
                )

                GenericEmptyStateLayout(
                    iconRes = R.drawable.network_error_icon,
                    title = stringResource(errorText.titleRes),
                    description = stringResource(errorText.descriptionRes),
                    iconTint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    actionButton = {
                        Button(
                            onClick = { items.retry() },
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                )
            }

            isTrulyEmpty -> {
                val trimmedQuery = query.trim()

                if (tab == Tab.FAVORITES) {
                    GenericEmptyStateLayout(
                        iconRes = R.drawable.favorites_nodata_icon,
                        title = "No favorites yet.",
                        description = "Articles you pin will appear here so you can easily read them later."
                    )
                } else if (trimmedQuery.isNotBlank()) {
                    GenericEmptyStateLayout(
                        iconRes = R.drawable.search_nodata_icon,
                        title = "No results found.",
                        description = "We couldn't find any articles matching \"$trimmedQuery\".\nTry a different keyword."
                    )
                } else {
                    GenericEmptyStateLayout(
                        iconRes = R.drawable.all_nodata_icon,
                        title = "No news available.",
                        description = "There are currently no articles published.\nPull down to refresh and check again."
                    )
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