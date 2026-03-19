package com.example.newsapp.domain.repository

import androidx.paging.PagingData
import com.example.newsapp.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface ArticleRepository {
    fun getArticles(search: String?): Flow<PagingData<Article>>
    fun getFavoriteArticles(): Flow<PagingData<Article>>
    fun getArticle(id: Long): Flow<Article?>
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
}

