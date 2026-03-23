package com.example.newsapp.ui.compose.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.newsapp.ui.Tab
import com.example.newsapp.core.helpers.asString
import com.example.newsapp.ui.viewmodel.AllArticlesViewModel
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.newsapp.domain.model.ArticleListLayout
import com.example.newsapp.domain.model.UserPreferences
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AllArticlesScreen(
    userPreferences: UserPreferences,
    onRequestLayout: (ArticleListLayout) -> Unit,
    onSelectArticle: (Long) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val viewModel: AllArticlesViewModel = hiltViewModel()
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collectLatest { message ->
            snackbarHostState.showSnackbar(message.asString(context))
        }
    }

    ArticleListPane(
        tab = Tab.ALL,
        showSearch = true,
        userPreferences = userPreferences, // Uses hoisted state
        searchQuery = searchQuery,
        articles = viewModel.articles,
        actions = ArticleListActions(
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
            onSelectArticle = onSelectArticle,
            onToggleFavorite = viewModel::onToggleFavorite,
            onRequestLayout = onRequestLayout // Uses hoisted action
        )
    )
}

