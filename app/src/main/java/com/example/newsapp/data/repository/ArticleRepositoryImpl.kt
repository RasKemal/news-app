package com.example.newsapp.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.newsapp.data.local.NewsDatabase
import com.example.newsapp.data.mapper.toDomain
import com.example.newsapp.data.paging.ArticleRemoteMediator
import com.example.newsapp.data.remote.ApiService
import com.example.newsapp.domain.model.Article
import com.example.newsapp.domain.repository.ArticleRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DEFAULT_PAGE_SIZE = 27

class ArticleRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val database: NewsDatabase
) : ArticleRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getArticles(search: String?): Flow<PagingData<Article>> {
        val pagingSourceFactory = {
            if (search.isNullOrBlank()) {
                database.articleDao().getArticles()
            } else {
                database.articleDao().searchArticles(search)
            }
        }

        return Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE,
                // Reduce how much we download before the first items render.
                initialLoadSize = DEFAULT_PAGE_SIZE,
                prefetchDistance = (DEFAULT_PAGE_SIZE / 2).coerceAtLeast(1),
                enablePlaceholders = true
            ),
            remoteMediator = ArticleRemoteMediator(
                search = search,
                apiService = apiService,
                database = database,
                pageSize = DEFAULT_PAGE_SIZE
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun getFavoriteArticles(): Flow<PagingData<Article>> {
        val pagingSourceFactory = { database.articleDao().getFavoriteArticles() }

        return Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE,
                initialLoadSize = DEFAULT_PAGE_SIZE,
                prefetchDistance = (DEFAULT_PAGE_SIZE / 2).coerceAtLeast(1),
                enablePlaceholders = true
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun getArticle(id: Long): Flow<Article?> =
        database.articleDao()
            .observeArticleById(id)
            .map { entity -> entity?.toDomain() }

    override suspend fun setFavorite(id: Long, isFavorite: Boolean) {
        database.articleDao().updateFavorite(id, isFavorite)
    }
}

