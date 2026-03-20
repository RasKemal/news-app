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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.shadow
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
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
import com.example.newsapp.ui.components.ArticleGridPlaceholder
import com.example.newsapp.ui.components.ArticleListItem
import com.example.newsapp.ui.components.ArticleListPlaceholder
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
    val appendState = items.loadState.append
    val isEmpty = items.itemCount == 0

    val refreshErrorMessage = (refreshState as? LoadState.Error)?.error?.message
        ?: "Failed to load articles"

    // 1. HOIST SCROLL STATES: This guarantees the scroll position is remembered across recompositions
    val listState = rememberLazyListState()
    val gridState = rememberLazyStaggeredGridState()

    Column(modifier = Modifier.fillMaxSize()) {
        // --- HEADER ---
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
                        .shadow(elevation = 10.dp, shape = shape, ambientColor = glow, spotColor = glow)
                        .background(bg, shape)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val listSelected = !isGrid
                    val gridSelected = isGrid

                    Row(
                        modifier = Modifier
                            .background(color = if (listSelected) MaterialTheme.colorScheme.surface else bg, shape = shape)
                            .clickable { if (!listSelected) onRequestLayout(ArticleListLayout.LIST) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.list_icon),
                            contentDescription = "List layout",
                            tint = if (listSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .background(color = if (gridSelected) MaterialTheme.colorScheme.surface else bg, shape = shape)
                            .clickable { if (!gridSelected) onRequestLayout(ArticleListLayout.GRID) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.gird_icon),
                            contentDescription = "Grid layout",
                            tint = if (gridSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (showSearch) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    placeholder = { Text("Search articles…") },
                    leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Search") },
                    singleLine = true,
                    shape = RoundedCornerShape(999.dp)
                )
            }
        }

        // --- BODY ---
        Box(modifier = Modifier.fillMaxSize()) {

            // 2. THE PERMANENT LIST TREE: Never unmount this!
            Column(modifier = Modifier.fillMaxSize()) {

                // Inline indicators stay at the top of the content
                if (!isEmpty) {
                    if (refreshState is LoadState.Error) {
                        Text(
                            text = refreshErrorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                    if (refreshState is LoadState.Loading) {
                        Text(
                            text = "Refreshing...",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                if (prefs.articleListLayout == ArticleListLayout.LIST) {
                    val showAppendLoading = appendState is LoadState.Loading
                    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                        items(
                            count = items.itemCount,
                            key = { index -> items[index]?.id ?: index },
                            contentType = { "article" } // 3. Massive performance boost!
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
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                } else {
                    val showAppendLoading = appendState is LoadState.Loading
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
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
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

            // 4. OVERLAYS: If the list is truly empty, we draw a solid background over the list
            if (isEmpty) {
                when {
                    refreshState is LoadState.Loading -> {
                        SpaceLoadingIndicator(
                            message = "Launching news…",
                            // Solid background hides the empty list entirely
                            modifier = Modifier.background(MaterialTheme.colorScheme.background)
                        )
                    }
                    refreshState is LoadState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = refreshErrorMessage, style = MaterialTheme.typography.bodyMedium)
                                Button(onClick = { items.retry() }, modifier = Modifier.padding(top = 16.dp)) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    refreshState is LoadState.NotLoading && appendState !is LoadState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            val trimmedQuery = query.trim()
                            val emptyText = when (tab) {
                                Tab.FAVORITES -> "No favorites yet"
                                Tab.ALL -> if (trimmedQuery.isNotBlank()) "No results for \"$trimmedQuery\"" else "No articles yet"
                            }
                            Text(text = emptyText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
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
