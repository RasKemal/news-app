package com.example.newsapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.newsapp.ui.viewmodel.ArticleDetailViewModel


@Composable
fun ArticleDetailScreen(
    articleId: Long,
    onBack: () -> Unit,
    showBackButton: Boolean,
    modifier: Modifier = Modifier
) {
    val viewModel: ArticleDetailViewModel = hiltViewModel()
    val uiState by viewModel.detailUiState.collectAsState()

    LaunchedEffect(articleId) {
        viewModel.setArticleId(articleId)
    }

    ArticleDetailPane(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onToggleFavorite = viewModel::onToggleFavorite,
        showBackButton = showBackButton
    )
}

