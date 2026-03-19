package com.example.newsapp.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.newsapp.domain.model.ArticleListLayout
import com.example.newsapp.domain.model.Article
import com.example.newsapp.ui.Tab
import com.example.newsapp.ui.components.ArticleListItem
import com.example.newsapp.ui.screens.ArticleListPane
import com.example.newsapp.ui.viewmodel.AllArticlesViewModel
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.MaterialTheme

@Composable
fun AllArticlesScreen(
    onSelectArticle: (Long) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val viewModel: AllArticlesViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    ArticleListPane(
        tab = Tab.ALL,
        showSearch = true,
        userPreferences = viewModel.userPreferences,
        searchQuery = viewModel.searchQuery,
        articles = viewModel.articles,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSelectArticle = onSelectArticle,
        onToggleFavorite = viewModel::onToggleFavorite,
        onRequestLayout = viewModel::onRequestLayout
    )
}

