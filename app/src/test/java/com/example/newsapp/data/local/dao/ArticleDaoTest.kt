package com.example.newsapp.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.newsapp.data.local.NewsDatabase
import com.example.newsapp.data.local.entity.ArticleEntity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ArticleDaoTest {

    private lateinit var database: NewsDatabase
    private lateinit var articleDao: ArticleDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, NewsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        articleDao = database.articleDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertAll persists all ArticleEntity rows`() = runBlocking {
        val entities = sampleArticles()

        articleDao.insertAll(entities)

        val loaded = articleDao.getArticlesByIds(entities.map { it.id })
        assertEquals(entities.size, loaded.size)
        assertEquals(entities.toSet(), loaded.toSet())
    }

    @Test
    fun `getArticles pagingSource returns rows ordered by publishedAt DESC`() = runBlocking {
        val older = baseArticle(id = 1L, publishedAt = "2024-01-01T10:00:00Z")
        val newer = baseArticle(id = 2L, publishedAt = "2026-03-19T10:00:00Z")
        val middle = baseArticle(id = 3L, publishedAt = "2025-06-15T10:00:00Z")
        articleDao.insertAll(listOf(older, newer, middle))

        val source = articleDao.getArticles()
        val result = source.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        ) as PagingSource.LoadResult.Page<Int, ArticleEntity>

        val ids = result.data.map { it.id }
        assertEquals(listOf(2L, 3L, 1L), ids)
    }

    @Test
    fun `clearAll removes every row from articles`() = runBlocking {
        articleDao.insertAll(sampleArticles())
        assertTrue(articleDao.getArticlesByIds(listOf(1L, 2L)).isNotEmpty())

        articleDao.clearAll()

        assertTrue(articleDao.getArticlesByIds(listOf(1L, 2L)).isEmpty())
    }

    @Test
    fun `searchArticles FTS and searchFavoriteArticles keyword filter return expected rows`() = runTest {
        val moonInTitle = baseArticle(
            id = 1L,
            publishedAt = "2026-03-19T10:00:00Z",
            title = "Moon mission update",
            summary = "Launch window is confirmed",
            isFavorite = false
        )
        val moonInSummaryFavorite = baseArticle(
            id = 2L,
            publishedAt = "2026-03-18T10:00:00Z",
            title = "SpaceX status",
            summary = "Moon payload integration complete",
            isFavorite = true
        )
        val unrelatedFavorite = baseArticle(
            id = 3L,
            publishedAt = "2026-03-17T10:00:00Z",
            title = "Mars rover",
            summary = "Drilling operations nominal",
            isFavorite = true
        )
        articleDao.insertAll(listOf(moonInTitle, moonInSummaryFavorite, unrelatedFavorite))

        val ftsResult = articleDao.searchArticles("moon").load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        ) as PagingSource.LoadResult.Page<Int, ArticleEntity>
        assertEquals(listOf(1L, 2L), ftsResult.data.map { it.id })

        val favoritesResult = articleDao.searchFavoriteArticles("moon").load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        ) as PagingSource.LoadResult.Page<Int, ArticleEntity>
        assertEquals(listOf(2L), favoritesResult.data.map { it.id })
    }

    private fun sampleArticles(): List<ArticleEntity> = listOf(
        baseArticle(id = 1L, publishedAt = "2026-01-01T00:00:00Z"),
        baseArticle(id = 2L, publishedAt = "2026-01-02T00:00:00Z")
    )

    private fun baseArticle(
        id: Long,
        publishedAt: String,
        title: String = "Title $id",
        summary: String = "Summary $id",
        isFavorite: Boolean = false
    ): ArticleEntity =
        ArticleEntity(
            id = id,
            title = title,
            summary = summary,
            url = "https://example.com/$id",
            imageUrl = null,
            newsSite = "Example",
            publishedAt = publishedAt,
            isFavorite = isFavorite
        )
}
