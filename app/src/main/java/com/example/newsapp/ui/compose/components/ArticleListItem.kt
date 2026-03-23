package com.example.newsapp.ui.compose.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.newsapp.R
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
                .padding(12.dp) // Uniform padding inside the card
            // verticalAlignment = Alignment.CenterVertically // **Removed this!**
        ) {
            // --- IMAGE SECTION ---
            ArticleImage(
                imageUrl = article.imageUrl,
                title = article.title,
                modifier = Modifier
                    .size(110.dp) // Fixed square size
                    .align(Alignment.CenterVertically) // Centered vertically relative to the Row
                    .clip(RoundedCornerShape(12.dp))
            )

            // --- TEXT SECTION ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp) // Breathing room between image and text
                    // Also centered vertically relative to the entire Row content
                    .align(Alignment.CenterVertically), // New alignment logic
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        // No maxLines! The title can grow as much as it needs to.
                    )
                    Text(
                        text = article.summary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2, // Keeps the summary from overwhelming the card
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!article.publishedAt.isNullOrBlank()) {
                    Column {
                        Text(
                            text = article.newsSite.orEmpty(),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = article.publishedAt,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.align(Alignment.Top) // Use size(36.dp) for the Grid item!
            ) {
                AnimatedContent(
                    targetState = article.isFavorite,
                    transitionSpec = {
                        // When the user pins the article (false -> true)
                        if (targetState) {
                            // Creates a bouncy "pop" effect
                            (scaleIn(
                                spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ) + fadeIn(tween(150))) togetherWith
                                    (scaleOut(tween(100)) + fadeOut(tween(100)))
                        } else {
                            // When unpinning (true -> false), just do a quick, subtle fade/scale
                            (scaleIn(tween(150)) + fadeIn(tween(150))) togetherWith
                                    (scaleOut(tween(150)) + fadeOut(tween(150)))
                        }
                    },
                    label = "pinAnimation"
                ) { isFav ->
                    // The icon draws based on the animated 'isFav' state, NOT the raw 'article.isFavorite'
                    Icon(
                        painter = painterResource(
                            id = if (isFav) R.drawable.pin_filled else R.drawable.pin_outlined
                        ),
                        contentDescription = if (isFav) {
                            stringResource(R.string.cd_unpin_article)
                        } else {
                            stringResource(R.string.cd_pin_article)
                        },
                        tint = if (isFav) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ArticleListPlaceholder() {
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp)
        ) {
            // --- FAKE IMAGE ---
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )

            // --- FAKE TEXT SECTION ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fake Title (2 lines)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(16.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }

                // Fake Summary (1 line)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                )

                // Fake Date/Metadata (1 short line)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleListItemPreview() {
    NewsAppTheme(darkTheme = true) {
        ArticleListItem(
            article = Article(
                id = 1L,
                title = "A incredibly massive spaceflight title that will definitely wrap to a second or even third line to test how the layout handles it",
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

