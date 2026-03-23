package com.example.newsapp.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.paging.AsyncPagingDataDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.newsapp.data.local.NewsDatabase
import com.example.newsapp.data.remote.ApiService
import com.example.newsapp.data.remote.dto.ArticleDto
import com.example.newsapp.data.remote.dto.NewsResponse
import app.cash.turbine.test
import com.example.newsapp.data.local.entity.ArticleEntity
import com.example.newsapp.domain.model.Article
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ArticleRepositoryTest {

    private lateinit var database: NewsDatabase
    private lateinit var apiService: ApiService
    private lateinit var repository: ArticleRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, NewsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        apiService = mockk()
        repository = ArticleRepositoryImpl(apiService, database)
    }

    @After
    fun tearDown() {
        database.close()
    }


    @Test
    fun `getArticles returns a Flow that emits PagingData`() = runTest {
        coEvery {
            apiService.getArticles(limit = any(), offset = any(), search = any())
        } returns NewsResponse(count = 0, next = null, previous = null, results = emptyList())

        repository.getArticles(search = null).test {
            assertNotNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
