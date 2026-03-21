package com.example.newsapp.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.newsapp.ui.Tab
import com.example.newsapp.ui.viewmodel.AllArticlesViewModel
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AllArticlesScreen(
    onSelectArticle: (Long) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val viewModel: AllArticlesViewModel = hiltViewModel()
    val userPrefs by viewModel.userPreferences.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    ArticleListPane(
        tab = Tab.ALL,
        showSearch = true,
        userPreferences = userPrefs,
        searchQuery = searchQuery,
        articles = viewModel.articles,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSelectArticle = onSelectArticle,
        onToggleFavorite = viewModel::onToggleFavorite,
        onRequestLayout = viewModel::onRequestLayout
    )
}

