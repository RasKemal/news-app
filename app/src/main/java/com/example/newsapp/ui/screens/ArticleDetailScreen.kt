package com.example.newsapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

    LaunchedEffect(articleId) {
        viewModel.setArticleId(articleId)
    }

    ArticleDetailPane(
        detailUiState = viewModel.detailUiState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onToggleFavorite = viewModel::onToggleFavorite,
        showBackButton = showBackButton
    )
}

