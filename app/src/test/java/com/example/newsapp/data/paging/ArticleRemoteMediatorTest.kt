package com.example.newsapp.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.newsapp.data.local.NewsDatabase
import com.example.newsapp.data.local.entity.ArticleEntity
import com.example.newsapp.data.local.entity.ArticleRemoteKeysEntity
import com.example.newsapp.data.local.entity.SearchMetadataEntity
import com.example.newsapp.data.remote.ApiService
import com.example.newsapp.data.remote.dto.ArticleDto
import com.example.newsapp.data.remote.dto.NewsResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ArticleRemoteMediatorTest {

    private lateinit var database: NewsDatabase
    private lateinit var apiService: ApiService

    private val pageSize = 24

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, NewsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        apiService = mockk()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `REFRESH Success endOfPaginationReached true when API returns empty list`() = runBlocking {
        coEvery { apiService.getArticles(any(), any(), any()) } returns NewsResponse(
            count = 0,
            next = null,
            previous = null,
            results = emptyList()
        )

        val mediator = ArticleRemoteMediator(null, apiService, database, pageSize)
        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        val success = result as RemoteMediator.MediatorResult.Success
        assertTrue(success.endOfPaginationReached)
        coVerify(exactly = 1) { apiService.getArticles(pageSize, 0, null) }
    }

    @Test
    fun `REFRESH returns Error when API throws`() = runBlocking {
        coEvery { apiService.getArticles(any(), any(), any()) } throws IOException("network down")

        val mediator = ArticleRemoteMediator(null, apiService, database, pageSize)
        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }

    @Test
    fun `initialize skips refresh when cache is fresh and remote keys exist`() = runBlocking {
        val searchKey = ""
        database.searchMetadataDao().insert(
            SearchMetadataEntity(searchKey, System.currentTimeMillis())
        )
        database.remoteKeysDao().insertAll(
            listOf(
                ArticleRemoteKeysEntity(
                    articleId = 1L,
                    searchQuery = searchKey,
                    prevOffset = null,
                    nextOffset = pageSize
                )
            )
        )

        val mediator = ArticleRemoteMediator(null, apiService, database, pageSize)
        val action = mediator.initialize()

        assertEquals(RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH, action)
        coVerify(exactly = 0) { apiService.getArticles(any(), any(), any()) }
    }

    @Test
    fun `initialize launches refresh when metadata is fresh but remote keys are missing`() =
        runBlocking {
            val searchKey = ""
            database.searchMetadataDao().insert(
                SearchMetadataEntity(searchKey, System.currentTimeMillis())
            )

            val mediator = ArticleRemoteMediator(null, apiService, database, pageSize)
            val action = mediator.initialize()

            assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, action)
        }

    @Test
    fun `initialize launches refresh when cache is expired even if remote keys exist`() = runBlocking {
        val searchKey = ""
        database.searchMetadataDao().insert(
            SearchMetadataEntity(
                searchKey,
                System.currentTimeMillis() - (60L * 60L * 1000L + 1L) // 1h + 1ms ago
            )
        )
        database.remoteKeysDao().insertAll(
            listOf(
                ArticleRemoteKeysEntity(
                    articleId = 1L,
                    searchQuery = searchKey,
                    prevOffset = null,
                    nextOffset = pageSize
                )
            )
        )

        val mediator = ArticleRemoteMediator(null, apiService, database, pageSize)
        val action = mediator.initialize()

        assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, action)
        coVerify(exactly = 0) { apiService.getArticles(any(), any(), any()) }
    }

    @Test
    fun `APPEND returns endOfPaginationReached when no remote key exists for last item`() =
        runBlocking {
            val entity = ArticleEntity(
                id = 99L,
                title = "T",
                summary = "S",
                url = "https://example.com/99",
                imageUrl = null,
                newsSite = "X",
                publishedAt = "2026-01-01T00:00:00Z",
                isFavorite = false
            )
            database.articleDao().insertAll(listOf(entity))

            val appendState = PagingState(
                pages = listOf(
                    PagingSource.LoadResult.Page(
                        data = listOf(entity),
                        prevKey = null,
                        nextKey = 24
                    )
                ),
                anchorPosition = 0,
                config = PagingConfig(pageSize = pageSize),
                leadingPlaceholderCount = 0
            )

            val mediator = ArticleRemoteMediator(null, apiService, database, pageSize)
            val result = mediator.load(LoadType.APPEND, appendState)

            assertTrue(result is RemoteMediator.MediatorResult.Success)
            val success = result as RemoteMediator.MediatorResult.Success
            assertTrue(success.endOfPaginationReached)
            coVerify(exactly = 0) { apiService.getArticles(any(), any(), any()) }
        }

    @Test
    fun `APPEND uses remote key nextOffset and inserts new page`() = runBlocking {
        val searchKey = ""
        val existing = ArticleEntity(
            id = 1L,
            title = "Existing",
            summary = "Existing",
            url = "https://example.com/1",
            imageUrl = null,
            newsSite = "X",
            publishedAt = "2026-01-01T00:00:00Z",
            isFavorite = false
        )
        database.articleDao().insertAll(listOf(existing))
        database.remoteKeysDao().insertAll(
            listOf(
                ArticleRemoteKeysEntity(
                    articleId = existing.id,
                    searchQuery = searchKey,
                    prevOffset = null,
                    nextOffset = pageSize
                )
            )
        )

        coEvery {
            apiService.getArticles(
                limit = pageSize,
                offset = pageSize,
                search = null
            )
        } returns NewsResponse(
            count = 2,
            next = null,
            previous = null,
            results = listOf(
                ArticleDto(
                    id = 2L,
                    title = "Page 2",
                    summary = "From append",
                    url = "https://example.com/2",
                    imageUrl = null,
                    newsSite = "Y",
                    publishedAt = "2026-01-02T00:00:00Z"
                )
            )
        )

        val appendState = PagingState(
            pages = listOf(
                PagingSource.LoadResult.Page(
                    data = listOf(existing),
                    prevKey = null,
                    nextKey = pageSize
                )
            ),
            anchorPosition = 0,
            config = PagingConfig(pageSize = pageSize),
            leadingPlaceholderCount = 0
        )

        val mediator = ArticleRemoteMediator(null, apiService, database, pageSize)
        val result = mediator.load(LoadType.APPEND, appendState)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        val success = result as RemoteMediator.MediatorResult.Success
        assertTrue(!success.endOfPaginationReached)
        coVerify(exactly = 1) {
            apiService.getArticles(
                limit = pageSize,
                offset = pageSize,
                search = null
            )
        }
        assertEquals(1, database.articleDao().getArticlesByIds(listOf(2L)).size)
        assertTrue(database.remoteKeysDao().remoteKeysByArticleId(2L, searchKey) != null)
    }

    @Test
    fun `PREPEND short-circuits and does not call API`() = runBlocking {
        val mediator = ArticleRemoteMediator(null, apiService, database, pageSize)
        val result = mediator.load(LoadType.PREPEND, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        val success = result as RemoteMediator.MediatorResult.Success
        assertTrue(success.endOfPaginationReached)
        coVerify(exactly = 0) { apiService.getArticles(any(), any(), any()) }
    }

    @Test
    fun `REFRESH Success endOfPaginationReached false when API returns data and DB is updated`() =
        runBlocking {
            val dto = ArticleDto(
                id = 1L,
                title = "T",
                summary = "S",
                url = "https://example.com/1",
                imageUrl = null,
                newsSite = "X",
                publishedAt = "2026-01-01T00:00:00Z"
            )
            coEvery { apiService.getArticles(any(), any(), any()) } returns NewsResponse(
                count = 1,
                next = null,
                previous = null,
                results = listOf(dto)
            )

            val mediator = ArticleRemoteMediator(null, apiService, database, pageSize)
            val result = mediator.load(LoadType.REFRESH, emptyPagingState())

            assertTrue(result is RemoteMediator.MediatorResult.Success)
            val success = result as RemoteMediator.MediatorResult.Success
            assertTrue(!success.endOfPaginationReached)

            val stored = database.articleDao().getArticlesByIds(listOf(1L))
            assertEquals(1, stored.size)
            assertEquals(1L, stored.first().id)

            val keys = database.remoteKeysDao().remoteKeysByArticleId(1L, "")
            assertTrue(keys != null)
        }

    @Test
    fun `REFRESH stores remote keys with correct searchQuery partition`() = runBlocking {
        val searchKey = "moon"
        val dto = ArticleDto(
            id = 10L,
            title = "T",
            summary = "S",
            url = "https://example.com/10",
            imageUrl = null,
            newsSite = "X",
            publishedAt = "2026-03-19T10:00:00Z"
        )

        coEvery { apiService.getArticles(limit = pageSize, offset = 0, search = searchKey) } returns NewsResponse(
            count = 1,
            next = null,
            previous = null,
            results = listOf(dto)
        )

        val mediator = ArticleRemoteMediator(searchKey, apiService, database, pageSize)
        val result = mediator.load(LoadType.REFRESH, emptyPagingState())
        assertTrue(result is RemoteMediator.MediatorResult.Success)

        val keys = database.remoteKeysDao().remoteKeysByArticleId(articleId = dto.id, search = searchKey)
        assertTrue(keys != null)
        assertEquals(dto.id, keys!!.articleId)
        assertEquals(searchKey, keys.searchQuery)
    }

    @Test
    fun `REFRESH only deletes articles that have no remaining remote keys`() = runBlocking {
        val globalArticle = ArticleEntity(1L, "Global", "", "", null, "", "", false)
        val marsArticle = ArticleEntity(2L, "Mars", "", "", null, "", "", false)
        database.articleDao().insertAll(listOf(globalArticle, marsArticle))

        database.remoteKeysDao().insertAll(listOf(
            ArticleRemoteKeysEntity(1L, "", null, 24),
            ArticleRemoteKeysEntity(2L, "Mars", null, 24)
        ))

        coEvery { apiService.getArticles(any(), any(), search = "") } returns NewsResponse(
            count = 0, next = null, previous = null, results = emptyList()
        )

        val mediator = ArticleRemoteMediator("", apiService, database, pageSize)
        mediator.load(LoadType.REFRESH, emptyPagingState())

        val remainingGlobal = database.articleDao().getArticlesByIds(listOf(1L))
        val remainingMars = database.articleDao().getArticlesByIds(listOf(2L))

        assertTrue("Global article should have been deleted", remainingGlobal.isEmpty())
        assertTrue("Mars article should have survived", remainingMars.isNotEmpty())
    }





    private fun emptyPagingState(): PagingState<Int, ArticleEntity> =
        PagingState(
            pages = emptyList(),
            anchorPosition = null,
            config = PagingConfig(pageSize = pageSize),
            leadingPlaceholderCount = 0
        )
}
