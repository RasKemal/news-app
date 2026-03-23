package com.example.newsapp.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.newsapp.R
import com.example.newsapp.domain.model.ArticleListLayout
import com.example.newsapp.ui.Tab

@Composable
fun ArticleListHeader(
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
                text = if (tab == Tab.ALL) {
                    stringResource(R.string.title_all_articles)
                } else {
                    stringResource(R.string.title_favorite_articles)
                },
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
                        contentDescription = stringResource(R.string.cd_list_layout),
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
                        contentDescription = stringResource(R.string.cd_grid_layout),
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
                placeholder = { Text(stringResource(R.string.placeholder_search_articles)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = stringResource(R.string.cd_search)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(999.dp)
            )
        }
    }
}