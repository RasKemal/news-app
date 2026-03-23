package com.example.newsapp.ui.compose.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.newsapp.R
import com.example.newsapp.ui.theme.NewsAppTheme

@Composable
fun GenericEmptyStateLayout(
    @DrawableRes iconRes: Int,
    title: String,
    description: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
    actionButton: (@Composable () -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = iconTint
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (actionButton != null) {
            Spacer(modifier = Modifier.height(24.dp))
            actionButton()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GenericEmptyStateLayoutPreview() {
    NewsAppTheme(darkTheme = true) {
        GenericEmptyStateLayout(
            iconRes = R.drawable.search_nodata_icon,
            title = "No results found.",
            description = "Try another keyword or clear search."
        )
    }
}