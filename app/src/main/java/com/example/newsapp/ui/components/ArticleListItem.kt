package com.example.newsapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.newsapp.domain.model.Article
import com.example.newsapp.ui.theme.NewsAppTheme

@Composable
fun ArticleListItem(
    article: Article,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick() }
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top
        ) {
            if (!article.imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                ) {
                    AsyncImage(
                        model = article.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clipToBounds(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!article.publishedAt.isNullOrBlank()) {
                    Column {
                        Text(
                            text = article.newsSite.orEmpty(),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = article.publishedAt,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Text(
                text = if (article.isFavorite) "★" else "☆",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onToggleFavorite() }
                    .padding(start = 8.dp)
                    .padding(vertical = 8.dp),
                color = if (article.isFavorite) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleListItemPreview() {
    NewsAppTheme {
        ArticleListItem(
            article = Article(
                id = 1L,
                title = "SpaceX launches next Starlink mission",
                summary = "Falcon 9 delivered a new batch of satellites to low Earth orbit successfully.",
                url = "https://example.com/article/1",
                imageUrl = "https://example.com/image.jpg",
                newsSite = "SpaceNews",
                publishedAt = "2026-03-17T12:00:00Z",
                isFavorite = true
            ),
            onClick = {},
            onToggleFavorite = {}
        )
    }
}

