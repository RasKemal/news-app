package com.example.newsapp.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.shadow
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.res.painterResource
import com.example.newsapp.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.newsapp.domain.model.Article
import com.example.newsapp.domain.model.ArticleListLayout
import com.example.newsapp.domain.model.UserPreferences
import com.example.newsapp.ui.Tab
import com.example.newsapp.ui.components.ArticleGridItem
import com.example.newsapp.ui.components.ArticleListItem
import com.example.newsapp.ui.state.UiState
import com.example.newsapp.ui.theme.NewsAppTheme

@Composable
fun ArticleListPane(
    tab: Tab,
    showSearch: Boolean,
    userPreferences: StateFlow<UserPreferences>,
    searchQuery: StateFlow<String>,
    articles: Flow<PagingData<Article>>,
    onSearchQueryChanged: (String) -> Unit,
    onSelectArticle: (Long) -> Unit,
    onToggleFavorite: (Article) -> Unit,
    onRequestLayout: (ArticleListLayout) -> Unit
) {
    val prefs by userPreferences.collectAsState()
    val query by searchQuery.collectAsState()

    val items = articles.collectAsLazyPagingItems()

    val refreshState = items.loadState.refresh
    val isEmpty = items.itemCount == 0

    val refreshErrorMessage = (refreshState as? LoadState.Error)?.error?.message
        ?: "Failed to load articles"

    // IMPORTANT: show loading whenever Paging is refreshing, even if itemCount temporarily hits 0.
    // This prevents the "loading -> empty -> loading" flicker on start.
    val listUiState: UiState<Unit> = when (refreshState) {
        is LoadState.Loading -> UiState.Loading
        is LoadState.Error -> UiState.Error(refreshErrorMessage)
        else -> UiState.Success(Unit)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (tab == Tab.ALL) "All Articles" else "Favorite Articles",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                val isGrid = prefs.articleListLayout == ArticleListLayout.GRID
                val shape = RoundedCornerShape(999.dp)
                val bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                val glow = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)

                Row(
                    modifier = Modifier
                        .shadow(
                            elevation = 10.dp,
                            shape = shape,
                            ambientColor = glow,
                            spotColor = glow
                        )
                        .background(bg, shape)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val listSelected = !isGrid
                    val gridSelected = isGrid

                    // List segment (left, default)
                    Row(
                        modifier = Modifier
                            .background(
                                color = if (listSelected)
                                    MaterialTheme.colorScheme.surface
                                else
                                    bg,
                                shape = shape
                            )
                            .clickable {
                                if (!listSelected) onRequestLayout(ArticleListLayout.LIST)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // You can swap this to your custom list icon when available
                        Icon(
                            painter = painterResource(R.drawable.list_icon),
                            contentDescription = "List layout",
                            tint = if (listSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Grid segment (right)
                    Row(
                        modifier = Modifier
                            .background(
                                color = if (gridSelected)
                                    MaterialTheme.colorScheme.surface
                                else
                                    bg,
                                shape = shape
                            )
                            .clickable {
                                if (!gridSelected) onRequestLayout(ArticleListLayout.GRID)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // You can swap this to your custom grid icon when available
                        Icon(
                            painter = painterResource(R.drawable.gird_icon),
                            contentDescription = "Grid layout",
                            tint = if (gridSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (showSearch) {
                OutlinedTextField(
                    value = query,
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

        // Body
        when (listUiState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                val message = (listUiState as UiState.Error).message
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = message, style = MaterialTheme.typography.bodyMedium)
                        Button(
                            onClick = { items.retry() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            is UiState.Success -> {
                if (refreshState is LoadState.Error && !isEmpty) {
                    Text(
                        text = refreshErrorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                if (refreshState is LoadState.Loading && !isEmpty) {
                    Text(
                        text = "Refreshing...",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                if (items.itemCount == 0) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        // For "All" tab we keep a spinner while paging stabilizes,
                        // since "real empty" is unlikely with offline-first + mediator.
                        if (tab == Tab.FAVORITES) {
                            Text(
                                text = "No favorites yet",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            CircularProgressIndicator()
                        }
                    }
                } else if (prefs.articleListLayout == ArticleListLayout.LIST) {
                    val appendState = items.loadState.append
                    val showAppendLoading = appendState is LoadState.Loading
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(
                            count = items.itemCount,
                            key = { index -> items[index]?.id ?: index }
                        ) { index ->
                            val article = items[index] ?: return@items
                            ArticleListItem(
                                article = article,
                                onClick = { onSelectArticle(article.id) },
                                onToggleFavorite = { onToggleFavorite(article) }
                            )
                        }
                        if (showAppendLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                } else {
                    val appendState = items.loadState.append
                    val showAppendLoading = appendState is LoadState.Loading
                    val gridCount = items.itemCount + if (showAppendLoading) 1 else 0
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            count = gridCount,
                            key = { index ->
                                if (index < items.itemCount) items[index]?.id ?: index else "append-loading"
                            }
                        ) { index ->
                            if (index >= items.itemCount) {
                                // Loading footer occupies one "cell" worth of space.
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                val article = items[index] ?: return@items
                                ArticleGridItem(
                                    article = article,
                                    onClick = { onSelectArticle(article.id) },
                                    onToggleFavorite = { onToggleFavorite(article) }
                                )
                            }
                        }
                    }
                }

                if (items.loadState.append is LoadState.Error) {
                    val appendError = items.loadState.append as LoadState.Error
                    Text(
                        text = appendError.error.message ?: "Failed to load more",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = false, widthDp = 400, heightDp = 800)
@Composable
private fun ArticleListPaneAllPreview() {
    val prefs = MutableStateFlow(UserPreferences(articleListLayout = ArticleListLayout.LIST))
    val query = MutableStateFlow("mars")
    val articles = listOf(
        Article(
            id = 1L,
            title = "Mars sample return mission advances",
            summary = "Engineers completed a key integration test for the next mission phase.",
            url = "https://example.com/a1",
            imageUrl = null,
            newsSite = "ESA",
            publishedAt = "2026-03-17T10:00:00Z",
            isFavorite = false
        ),
        Article(
            id = 2L,
            title = "New propulsion milestone reached",
            summary = "A private company reported a successful long-duration engine firing.",
            url = "https://example.com/a2",
            imageUrl = null,
            newsSite = "SpaceNews",
            publishedAt = "2026-03-17T11:00:00Z",
            isFavorite = true
        )
    )
    NewsAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ArticleListPane(
                tab = Tab.ALL,
                showSearch = true,
                userPreferences = prefs,
                searchQuery = query,
                articles = flowOf(PagingData.from(articles)),
                onSearchQueryChanged = {},
                onSelectArticle = {},
                onToggleFavorite = {},
                onRequestLayout = {}
            )
        }
    }
}

@Preview(showBackground = false, widthDp = 400, heightDp = 800)
@Composable
private fun ArticleListPaneFavoritesGridPreview() {
    val prefs = MutableStateFlow(UserPreferences(articleListLayout = ArticleListLayout.GRID))
    val query = MutableStateFlow("")
    val favorites = listOf(
        Article(
            id = 10L,
            title = "Falcon 9 deploys payload",
            summary = "Mission completed successfully after nominal stage separation.",
            url = "https://example.com/f1",
            imageUrl = null,
            newsSite = "Spaceflight Now",
            publishedAt = "2026-03-17T13:00:00Z",
            isFavorite = true
        ),
        Article(
            id = 11L,
            title = "Lunar lander test campaign",
            summary = "Ground test campaign validated avionics and propulsion interfaces.",
            url = "https://example.com/f2",
            imageUrl = null,
            newsSite = "NASA",
            publishedAt = "2026-03-16T08:00:00Z",
            isFavorite = true
        )
    )
    NewsAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ArticleListPane(
                tab = Tab.FAVORITES,
                showSearch = false,
                userPreferences = prefs,
                searchQuery = query,
                articles = flowOf(PagingData.from(favorites)),
                onSearchQueryChanged = {},
                onSelectArticle = {},
                onToggleFavorite = {},
                onRequestLayout = {}
            )
        }
    }
}

