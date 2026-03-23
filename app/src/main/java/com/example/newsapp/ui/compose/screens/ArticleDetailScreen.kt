package com.example.newsapp.ui.compose.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.newsapp.R
import com.example.newsapp.core.helpers.UiState
import com.example.newsapp.domain.model.Article
import com.example.newsapp.ui.compose.components.ArticleImage
import com.example.newsapp.ui.compose.components.DetailFavoriteButton
import com.example.newsapp.ui.compose.components.DetailShareButton
import com.example.newsapp.ui.compose.components.GenericEmptyStateLayout
import com.example.newsapp.ui.viewmodel.ArticleDetailViewModel
import com.example.newsapp.ui.theme.NewsAppTheme

@Composable
fun ArticleDetailScreen(
    articleId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ArticleDetailViewModel = hiltViewModel()
    val uiState by viewModel.detailUiState.collectAsStateWithLifecycle()

    LaunchedEffect(articleId) {
        viewModel.setArticleId(articleId)
    }

    when (uiState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                GenericEmptyStateLayout(
                    iconRes = R.drawable.network_error_icon,
                    title = stringResource(R.string.article_detail_error_title),
                    description = stringResource(R.string.article_detail_error_description),
                    iconTint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    actionButton = {
                        Button(
                            onClick = viewModel::retry,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(stringResource(R.string.action_retry))
                        }
                    }
                )
            }
        }
        is UiState.Success -> {
            ArticleDetailContent(
                article = (uiState as UiState.Success).data,
                onBack = onBack,
                onToggleFavorite = viewModel::onToggleFavorite,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ArticleDetailContent(
    article: Article,
    onBack: () -> Unit,
    onToggleFavorite: (Article) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.detail_close_icon),
                    contentDescription = stringResource(R.string.cd_close),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        ArticleImage(
            imageUrl = article.imageUrl,
            title = article.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                if (!article.newsSite.isNullOrBlank()) {
                    Text(
                        text = article.newsSite,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary, // Accent color for the publisher
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                if (!article.publishedAt.isNullOrBlank()) {
                    Text(
                        text = article.publishedAt,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetailFavoriteButton(
                    isPinned = article.isFavorite,
                    onClick = { onToggleFavorite(article) }
                )

                DetailShareButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TITLE, article.title)
                            putExtra(
                                Intent.EXTRA_TEXT,
                                context.getString(R.string.share_article_message, article.title, article.url)
                            )
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(
                            sendIntent,
                            context.getString(R.string.share_article_chooser_title)
                        )
                        context.startActivity(shareIntent)
                    }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ArticleDetailContentPreview() {
    NewsAppTheme(darkTheme = true) {
        ArticleDetailContent(
            article = Article(
                id = 1L,
                title = "Falcon 9 launches new payload",
                summary = "SpaceX successfully launched a new payload to orbit after a smooth countdown and nominal ascent profile.",
                url = "https://example.com/article/1",
                imageUrl = null,
                newsSite = "SpaceNews",
                publishedAt = "Mar 19, 2026 • 12:00",
                isFavorite = false
            ),
            onBack = {},
            onToggleFavorite = {}
        )
    }
}

