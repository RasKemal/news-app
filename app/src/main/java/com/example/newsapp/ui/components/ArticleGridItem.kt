package com.example.newsapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.newsapp.R
import com.example.newsapp.domain.model.Article

@Composable
fun ArticleGridItem(
    article: Article,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            // --- IMAGE SECTION ---
            if (!article.imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                ) {
                    AsyncImage(
                        model = article.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // --- TEXT SECTION ---
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 3, 
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // --- METADATA & PIN ICON ROW ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom // Aligns the pin neatly with the text
                ) {
                    Column(
                        modifier = Modifier.weight(1f) // Prevents long site names from squishing the pin
                    ) {
                        Text(
                            text = article.newsSite.orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!article.publishedAt.isNullOrBlank()) {
                            Text(
                                text = article.publishedAt,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }


                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(36.dp) // Use size(36.dp) for the Grid item!
                    ) {
                        AnimatedContent(
                            targetState = article.isFavorite,
                            transitionSpec = {
                                // When the user pins the article (false -> true)
                                if (targetState) {
                                    // Creates a bouncy "pop" effect
                                    (scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) + fadeIn(tween(150))) togetherWith
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
                                contentDescription = if (isFav) "Unpin article" else "Pin article",
                                tint = if (isFav) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleGridPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // --- FAKE IMAGE ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )

            // --- FAKE TEXT SECTION ---
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fake Title
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().height(14.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp)))
                    Box(modifier = Modifier.fillMaxWidth(0.8f).height(14.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp)))
                }

                // Fake Summary
                Box(modifier = Modifier.fillMaxWidth().height(12.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp)))

                // Fake Date/Metadata
                Box(modifier = Modifier.fillMaxWidth(0.5f).height(12.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp)))
            }
        }
    }
}

