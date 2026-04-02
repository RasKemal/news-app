package com.example.newsapp.ui.compose.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.compose.LazyPagingItems
import com.example.newsapp.R
import kotlinx.coroutines.flow.Flow
import com.example.newsapp.domain.model.Article
import com.example.newsapp.domain.model.ArticleListLayout
import com.example.newsapp.domain.model.UserPreferences
import com.example.newsapp.ui.Tab
import com.example.newsapp.ui.compose.components.ArticleGridItem
import com.example.newsapp.ui.compose.components.ArticleGridPlaceholder
import com.example.newsapp.ui.compose.components.ArticleListItem
import com.example.newsapp.ui.compose.components.ArticleListPlaceholder
import com.example.newsapp.ui.compose.components.GenericEmptyStateLayout
import com.example.newsapp.ui.compose.components.PaginationErrorIndicator
import com.example.newsapp.ui.theme.NewsAppTheme
import com.example.newsapp.core.helpers.ErrorText
import com.example.newsapp.core.helpers.toUIError
import com.example.newsapp.ui.compose.components.ArticleListHeader
import kotlinx.coroutines.delay

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
    // Derive the refreshing state directly from Paging 3 engine
    val isRefreshing = refreshState is LoadState.Loading && items.itemCount > 0

    Column(modifier = Modifier.fillMaxSize()) {

        // HEADER
        ArticleListHeader(
            tab = tab,
            showSearch = showSearch,
            searchQuery = searchQuery,
            isListLayout = isListLayout,
            onSearchQueryChanged = actions.onSearchQueryChanged,
            onRequestLayout = actions.onRequestLayout
        )

        // CONDITIONALLY REFRESHABLE BODY
        val bodyModifier = Modifier.weight(1f)

        if (tab == Tab.ALL) {
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
            // No refresh functionality on favorites screen
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

                        // If pagination failed due to network
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

                        // If pagination failed due to network
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

        // OVERLAYS & EMPTY STATES
        val isEmpty = items.itemCount == 0
        val isMediatorLoading = items.loadState.mediator?.refresh is LoadState.Loading
        val isSourceLoading = items.loadState.source.refresh is LoadState.Loading
        val isLoading = isMediatorLoading || isSourceLoading
        val isTrulyEmpty = if (tab == Tab.FAVORITES) {
            items.loadState.source.refresh is LoadState.NotLoading
        } else {
            items.loadState.source.refresh is LoadState.NotLoading &&
                    items.loadState.mediator?.refresh is LoadState.NotLoading
        }
        val hasError =
            refreshState is LoadState.Error || items.loadState.mediator?.refresh is LoadState.Error


        if (isEmpty) {
            EmptyStateOverlay(
                hasError = hasError,
                isLoading = isLoading,
                isTrulyEmpty = isTrulyEmpty,
                refreshState = refreshState,
                items = items,
                tab = tab,
                query = searchQuery
            )
        } else if(hasError && !isLoading) {
            // Error Pill for background refresh failures
            if (refreshState is LoadState.Error) {
                val errorText = refreshState.error.toUIError()

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

// EXTRACTED SUB-COMPONENTS


@Composable
private fun EmptyStateOverlay(
    hasError: Boolean,
    isLoading: Boolean,
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
            isLoading -> {
                val message = if (query.trim().isNotBlank()) {
                    stringResource(R.string.loading_searching)
                } else {
                    stringResource(R.string.loading_articles)
                }
                SpaceLoadingIndicator(message = message)
            }
            hasError -> {
                val error = (refreshState as? LoadState.Error)?.error
                    ?: (items.loadState.mediator?.refresh as? LoadState.Error)?.error

                val errorText = error?.toUIError() ?: ErrorText(
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
                            Text(stringResource(R.string.action_retry))
                        }
                    }
                )
            }

            isTrulyEmpty -> {
                val trimmedQuery = query.trim()

                // check search failure first
                if (trimmedQuery.isNotBlank()) {
                    GenericEmptyStateLayout(
                        iconRes = R.drawable.search_nodata_icon,
                        title = stringResource(R.string.empty_no_results_title),
                        description = stringResource(
                            R.string.empty_no_results_description,
                            trimmedQuery
                        )
                    )
                }
                // if not searching, check if it's the Favorites tab
                else if (tab == Tab.FAVORITES) {
                    GenericEmptyStateLayout(
                        iconRes = R.drawable.favorites_nodata_icon,
                        title = stringResource(R.string.empty_no_favorites_title),
                        description = stringResource(R.string.empty_no_favorites_description)
                    )
                }
                // if not searching and on the All tab
                else {
                    GenericEmptyStateLayout(
                        iconRes = R.drawable.all_nodata_icon,
                        title = stringResource(R.string.empty_no_news_title),
                        description = stringResource(R.string.empty_no_news_description)
                    )
                }
            }

            else -> {
                val message = if (query.trim().isNotBlank()) {
                    stringResource(R.string.loading_searching)
                } else {
                    stringResource(R.string.loading_articles)
                }
                SpaceLoadingIndicator(message = message)
            }
        }
    }
}

@Composable
private fun SpaceLoadingIndicator(
    message: String,
    modifier: Modifier = Modifier,
    delayMillis: Long = 250L // delay for 250ms to prevent the loading spinner from flickering on fast connections
) {
    val isPreview = LocalInspectionMode.current
    var showLoading by remember { mutableStateOf(isPreview) }

    LaunchedEffect(Unit) {
        val effectiveDelay = if (isPreview) 0L else delayMillis
        delay(effectiveDelay)
        showLoading = true
    }

    if (showLoading) {
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
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ArticleListPanePreviewAllList() {
    NewsAppTheme(darkTheme = true) {
        val previewArticles = listOf(
            Article(
                id = 1L,
                title = "Starship static fire completed",
                summary = "SpaceX completed another successful static fire test ahead of the next launch window.",
                url = "https://example.com/1",
                imageUrl = null,
                newsSite = "Space.com",
                publishedAt = "Mar 19, 2026 • 11:30",
                isFavorite = false
            ),
            Article(
                id = 2L,
                title = "NASA publishes new Artemis roadmap",
                summary = "The latest roadmap outlines mission cadence and lunar surface operations updates.",
                url = "https://example.com/2",
                imageUrl = null,
                newsSite = "NASA",
                publishedAt = "Mar 19, 2026 • 09:10",
                isFavorite = true
            )
        )
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ArticleListHeader(
                    tab = Tab.ALL,
                    showSearch = true,
                    searchQuery = "",
                    isListLayout = true,
                    onSearchQueryChanged = {},
                    onRequestLayout = {}
                )
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(previewArticles.size) { index ->
                        val article = previewArticles[index]
                        ArticleListItem(
                            article = article,
                            onClick = {},
                            onToggleFavorite = {}
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ArticleListPanePreviewFavoritesEmpty() {
    NewsAppTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ArticleListHeader(
                    tab = Tab.FAVORITES,
                    showSearch = true,
                    searchQuery = "",
                    isListLayout = false,
                    onSearchQueryChanged = {},
                    onRequestLayout = {}
                )
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    GenericEmptyStateLayout(
                        iconRes = R.drawable.favorites_nodata_icon,
                        title = stringResource(R.string.empty_no_favorites_title),
                        description = stringResource(R.string.empty_no_favorites_description)
                    )
                }
            }
        }
    }
}