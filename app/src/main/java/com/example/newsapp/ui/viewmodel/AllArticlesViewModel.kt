package com.example.newsapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.newsapp.R
import com.example.newsapp.core.helpers.prepareForSearch
import com.example.newsapp.core.helpers.UiText
import com.example.newsapp.domain.model.Article
import com.example.newsapp.domain.usecase.GetArticlesUseCase
import com.example.newsapp.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class AllArticlesViewModel @Inject constructor(
    private val getArticlesUseCase: GetArticlesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    private val _snackbarMessages = MutableSharedFlow<UiText>(extraBufferCapacity = 1)
    val snackbarMessages = _snackbarMessages.asSharedFlow()

    private val searchQueryForRepo: Flow<String?> = _searchQuery.prepareForSearch()

    val articles: Flow<PagingData<Article>> =
        searchQueryForRepo
            .flatMapLatest { query -> getArticlesUseCase(query) }
            .cachedIn(viewModelScope)

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onToggleFavorite(article: Article) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(article.id, !article.isFavorite)
            } catch (t: Throwable) {
                _snackbarMessages.emit(
                    t.message?.let { UiText.DynamicString(it) }
                        ?: UiText.StringResource(R.string.error_update_favorite_failed)
                )
            }
        }
    }
}

