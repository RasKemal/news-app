package com.example.newsapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.R
import com.example.newsapp.core.helpers.UiText
import com.example.newsapp.domain.model.ArticleListLayout
import com.example.newsapp.domain.model.UserPreferences
import com.example.newsapp.domain.usecase.ObserveUserPreferencesUseCase
import com.example.newsapp.domain.usecase.SetArticleListLayoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    observeUserPreferencesUseCase: ObserveUserPreferencesUseCase,
    private val setArticleListLayoutUseCase: SetArticleListLayoutUseCase
) : ViewModel() {

    val userPreferences = observeUserPreferencesUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UserPreferences()
    )

    private val _snackbarMessages = MutableSharedFlow<UiText>(extraBufferCapacity = 1)
    val snackbarMessages = _snackbarMessages.asSharedFlow()

    fun setLayout(layout: ArticleListLayout) {
        viewModelScope.launch {
            try {
                setArticleListLayoutUseCase(layout)
            } catch (t: Throwable) {
                _snackbarMessages.emit(
                    t.message?.let { UiText.DynamicString(it) }
                        ?: UiText.StringResource(R.string.error_update_layout_failed)
                )
            }
        }
    }
}