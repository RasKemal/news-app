package com.example.newsapp.domain.usecase

import com.example.newsapp.domain.model.ArticleListLayout
import com.example.newsapp.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetArticleListLayoutUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(layout: ArticleListLayout) {
        repository.setArticleListLayout(layout)
    }
}

