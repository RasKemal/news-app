package com.example.newsapp.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.SnackbarHostState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.unit.dp
import com.example.newsapp.domain.model.ArticleListLayout
import com.example.newsapp.ui.Tab
import com.example.newsapp.ui.components.ArticleListItem
import com.example.newsapp.ui.screens.ArticleListPane
import com.example.newsapp.ui.viewmodel.FavoritesViewModel

import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.layout.Box
import com.example.newsapp.ui.state.UiState
import com.example.newsapp.domain.model.Article
import com.example.newsapp.domain.model.UserPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.remember

@Composable
fun FavoritesScreen(
    onSelectArticle: (Long) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val viewModel: FavoritesViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    ArticleListPane(
        tab = Tab.FAVORITES,
        showSearch = false,
        userPreferences = viewModel.userPreferences,
        searchQuery = viewModel.searchQuery,
        articles = viewModel.articles,
        onSearchQueryChanged = {},
        onSelectArticle = onSelectArticle,
        onToggleFavorite = viewModel::onToggleFavorite,
        onRequestLayout = viewModel::onRequestLayout
    )
}

