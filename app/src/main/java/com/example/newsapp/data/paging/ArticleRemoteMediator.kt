package com.example.newsapp.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.newsapp.data.local.NewsDatabase
import com.example.newsapp.data.local.entity.ArticleEntity
import com.example.newsapp.data.local.entity.ArticleRemoteKeys
import com.example.newsapp.data.mapper.toEntity
import com.example.newsapp.data.remote.ApiService
import com.example.newsapp.data.remote.dto.ArticleDto

@OptIn(ExperimentalPagingApi::class)
class ArticleRemoteMediator(
    private val search: String?,
    private val apiService: ApiService,
    private val database: NewsDatabase,
    private val pageSize: Int
) : RemoteMediator<Int, ArticleEntity>() {
    override suspend fun initialize(): InitializeAction {
        // Offline-first: if we already have mediator state for this query, skip the initial refresh so
        // the UI can immediately render cached Room data (and avoid blocking with network errors).
        val searchKey = search.orEmpty()
        val hasRemoteKeysForQuery = database.remoteKeysDao().hasRemoteKeysForQuery(searchKey)
        return if (hasRemoteKeysForQuery) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        return try {
            val searchKey = search.orEmpty()

            val offset = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) 0
                    else {
                        val remoteKeys = database.remoteKeysDao()
                            .remoteKeysByArticleId(lastItem.id, searchKey)
                        remoteKeys?.nextOffset ?: return MediatorResult.Success(endOfPaginationReached = true)
                    }
                }
            }

            val response = apiService.getArticles(
                limit = pageSize,
                offset = offset,
                search = search
            )

            val dtos = response.results
            val endReached = dtos.isEmpty()

            database.withTransaction {
                val articleDao = database.articleDao()
                val remoteKeysDao = database.remoteKeysDao()

                if (loadType == LoadType.REFRESH) {
                    remoteKeysDao.clearRemoteKeysForQuery(searchKey)
                }

                val ids = dtos.map { it.id }
                val existing = if (ids.isNotEmpty()) {
                    articleDao.getArticlesByIds(ids).associateBy { it.id }
                } else emptyMap<Long, ArticleEntity>()

                val prevOffset = if (offset == 0) null else offset - pageSize
                val nextOffset = if (endReached) null else offset + pageSize

                val entities = dtos.map { dto: ArticleDto ->
                    val existingFavorite = existing[dto.id]?.isFavorite ?: false
                    dto.toEntity(isFavorite = existingFavorite)
                }

                val keys = entities.map { entity ->
                    ArticleRemoteKeys(
                        articleId = entity.id,
                        searchQuery = searchKey,
                        prevOffset = prevOffset,
                        nextOffset = nextOffset
                    )
                }

                remoteKeysDao.insertAll(keys)
                articleDao.insertAll(entities)
            }

            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (t: Throwable) {
            MediatorResult.Error(t)
        }
    }
}

