package com.example.newsapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.newsapp.domain.model.Article
import com.example.newsapp.domain.model.ArticleListLayout
import com.example.newsapp.domain.model.UserPreferences
import com.example.newsapp.domain.usecase.GetArticlesUseCase
import com.example.newsapp.domain.usecase.ObserveUserPreferencesUseCase
import com.example.newsapp.domain.usecase.SetArticleListLayoutUseCase
import com.example.newsapp.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class AllArticlesViewModel @Inject constructor(
    observeUserPreferencesUseCase: ObserveUserPreferencesUseCase,
    private val setArticleListLayoutUseCase: SetArticleListLayoutUseCase,
    private val getArticlesUseCase: GetArticlesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbarMessages = _snackbarMessages.asSharedFlow()

    val userPreferences: StateFlow<UserPreferences> =
        observeUserPreferencesUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UserPreferences()
            )

    private val searchQueryForRepo: Flow<String?> =
        _searchQuery
            .map { raw ->
                val trimmed = raw.trim()
                // FTS MATCH is sensitive to query syntax. Keep it simple: allow letters/numbers/spaces.
                val normalized = trimmed
                    .replace(Regex("[^\\p{L}\\p{N} ]+"), " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                normalized
            }
            .debounce(300)
            .distinctUntilChanged()
            .map { it.ifBlank { null } }

    val articles: Flow<PagingData<Article>> =
        searchQueryForRepo
            .flatMapLatest { query -> getArticlesUseCase(query) }
            .cachedIn(viewModelScope)

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

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

