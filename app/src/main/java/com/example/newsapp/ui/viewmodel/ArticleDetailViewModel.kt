package com.example.newsapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.domain.common.Result as DomainResult
import com.example.newsapp.domain.model.Article
import com.example.newsapp.domain.usecase.GetArticleDetailUseCase
import com.example.newsapp.domain.usecase.ToggleFavoriteUseCase
import com.example.newsapp.ui.helper.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val getArticleDetailUseCase: GetArticleDetailUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _articleId = MutableStateFlow<Long?>(null)
    private val _retryToken = MutableStateFlow(0)
    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 1)

    val detailUiState: StateFlow<UiState<Article>> =
        combine(_articleId, _retryToken) { id, _ -> id }
            .flatMapLatest { id ->
                if (id == null) {
                    flowOf(UiState.Loading)
                } else {
                    getArticleDetailUseCase(id).map { result ->
                        when (result) {
                            is DomainResult.Loading -> UiState.Loading
                            is DomainResult.Success -> UiState.Success(result.data)
                            is DomainResult.Error -> UiState.Error(result.message)
                        }
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState.Loading
            )

    fun setArticleId(id: Long) {
        _articleId.value = id
    }

    fun retry() {
        _retryToken.update { it + 1 }
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

