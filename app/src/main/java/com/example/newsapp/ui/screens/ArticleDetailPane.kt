package com.example.newsapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.newsapp.domain.model.Article
import com.example.newsapp.ui.state.UiState
import com.example.newsapp.ui.theme.NewsAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ArticleDetailPane(
    detailUiState: StateFlow<UiState<Article>>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleFavorite: (Article) -> Unit,
    showBackButton: Boolean
) {
    val uiState by detailUiState.collectAsState()

    when (uiState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UiState.Error -> {
            val message = (uiState as UiState.Error).message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = message, style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        is UiState.Success -> {
            val article = (uiState as UiState.Success).data
            ArticleDetailContent(
                article = article,
                onBack = onBack,
                onToggleFavorite = onToggleFavorite,
                showBackButton = showBackButton
            )
        }
    }
}

@Composable
private fun ArticleDetailContent(
    article: Article,
    onBack: () -> Unit,
    onToggleFavorite: (Article) -> Unit,
    showBackButton: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (showBackButton) {
            OutlinedButton(onClick = onBack) {
                Text("Back")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (!article.publishedAt.isNullOrBlank()) {
            Text(
                text = article.publishedAt,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (!article.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = article.summary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = { onToggleFavorite(article) }) {
                Text(if (article.isFavorite) "Unfavorite" else "Favorite")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleDetailPaneSuccessPreview() {
    val state = MutableStateFlow<UiState<Article>>(
        UiState.Success(
            Article(
                id = 1L,
                title = "NASA shares Artemis mission update",
                summary = "NASA announced timeline details for upcoming Artemis milestones and hardware tests.",
                url = "https://example.com/article/2",
                imageUrl = null,
                newsSite = "NASA",
                publishedAt = "2026-03-17T09:00:00Z",
                isFavorite = false
            )
        )
    )
    NewsAppTheme {
        ArticleDetailPane(
            detailUiState = state,
            onBack = {},
            onRetry = {},
            onToggleFavorite = {},
            showBackButton = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleDetailPaneLoadingPreview() {
    val state = MutableStateFlow<UiState<Article>>(UiState.Loading)
    NewsAppTheme {
        ArticleDetailPane(
            detailUiState = state,
            onBack = {},
            onRetry = {},
            onToggleFavorite = {},
            showBackButton = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleDetailPaneErrorPreview() {
    val state = MutableStateFlow<UiState<Article>>(UiState.Error("Failed to load detail"))
    NewsAppTheme {
        ArticleDetailPane(
            detailUiState = state,
            onBack = {},
            onRetry = {},
            onToggleFavorite = {},
            showBackButton = true
        )
    }
}

