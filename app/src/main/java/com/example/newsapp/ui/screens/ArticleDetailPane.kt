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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.newsapp.domain.model.Article
import com.example.newsapp.ui.state.UiState

@Composable
fun ArticleDetailPane(
    uiState: UiState<Article>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleFavorite: (Article) -> Unit,
    showBackButton: Boolean
) {

    when (uiState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = uiState.message, style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Retry")
                    }
                }
            }
        }
        is UiState.Success -> {
            ArticleDetailContent(
                article = uiState.data,
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

