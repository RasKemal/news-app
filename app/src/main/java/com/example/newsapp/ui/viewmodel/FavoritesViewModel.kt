package com.example.newsapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.newsapp.domain.model.Article
import com.example.newsapp.domain.model.ArticleListLayout
import com.example.newsapp.domain.model.UserPreferences
import com.example.newsapp.domain.usecase.GetFavoriteArticlesUseCase
import com.example.newsapp.domain.usecase.ObserveUserPreferencesUseCase
import com.example.newsapp.domain.usecase.SetArticleListLayoutUseCase
import com.example.newsapp.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    observeUserPreferencesUseCase: ObserveUserPreferencesUseCase,
    private val setArticleListLayoutUseCase: SetArticleListLayoutUseCase,
    getFavoriteArticlesUseCase: GetFavoriteArticlesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val userPreferences: StateFlow<UserPreferences> =
        observeUserPreferencesUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UserPreferences()
            )

    val articles: Flow<androidx.paging.PagingData<Article>> =
        getFavoriteArticlesUseCase()
            .cachedIn(viewModelScope)

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbarMessages = _snackbarMessages.asSharedFlow()

    fun onRequestLayout(layout: ArticleListLayout) {
        viewModelScope.launch {
            try {
                setArticleListLayoutUseCase(layout)
            } catch (t: Throwable) {
                _snackbarMessages.emit(t.message ?: "Failed to update layout")
            }
        }
    }

    fun onToggleFavorite(article: Article) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(article.id, !article.isFavorite)
            } catch (t: Throwable) {
                _snackbarMessages.emit(t.message ?: "Failed to update favorite")
            }
        }
    }
}

