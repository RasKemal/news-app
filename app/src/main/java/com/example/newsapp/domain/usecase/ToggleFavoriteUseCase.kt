package com.example.newsapp.domain.usecase

import com.example.newsapp.domain.repository.ArticleRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(id: Long, isFavorite: Boolean) {
        repository.setFavorite(id, isFavorite)
    }
}

