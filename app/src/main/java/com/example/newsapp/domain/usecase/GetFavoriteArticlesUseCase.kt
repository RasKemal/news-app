package com.example.newsapp.domain.usecase

import androidx.paging.PagingData
import com.example.newsapp.domain.model.Article
import com.example.newsapp.domain.repository.ArticleRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetFavoriteArticlesUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    operator fun invoke(query: String? = null): Flow<PagingData<Article>> =
        repository.getFavoriteArticles(query)
}

