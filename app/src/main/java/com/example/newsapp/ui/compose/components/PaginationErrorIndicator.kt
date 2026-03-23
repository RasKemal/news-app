package com.example.newsapp.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.newsapp.R
import com.example.newsapp.core.helpers.toUIError
import com.example.newsapp.ui.theme.NewsAppTheme

@Composable
fun PaginationErrorIndicator(
    error: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val errorText = error.toUIError()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(id = errorText.titleRes),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        TextButton(onClick = onRetry) {
            Text(
                text = stringResource(R.string.action_retry),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PaginationErrorIndicatorPreview() {
    NewsAppTheme(darkTheme = true) {
        PaginationErrorIndicator(
            error = RuntimeException("Network error"),
            onRetry = {}
        )
    }
}