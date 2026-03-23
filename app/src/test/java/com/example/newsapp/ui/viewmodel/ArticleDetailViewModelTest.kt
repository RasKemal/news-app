package com.example.newsapp.ui.viewmodel

import app.cash.turbine.test
import com.example.newsapp.core.helpers.Result
import com.example.newsapp.core.helpers.UiState
import com.example.newsapp.domain.model.Article
import com.example.newsapp.domain.usecase.GetArticleDetailUseCase
import com.example.newsapp.domain.usecase.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.lang.RuntimeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getArticleDetailUseCase: GetArticleDetailUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getArticleDetailUseCase = mockk()
        toggleFavoriteUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial detailUiState is Loading when no id is set`() = runTest {
        val viewModel = ArticleDetailViewModel(
            getArticleDetailUseCase = getArticleDetailUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase
        )

        viewModel.detailUiState.test {
            assertEquals(UiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setArticleId eventually exposes Success UiState`() = runTest {
        val article = Article(
            id = 1L,
            title = "t",
            summary = "s",
            url = "https://example.com/1",
            imageUrl = null,
            newsSite = "X",
            publishedAt = "2026-01-01T00:00:00Z",
            isFavorite = false
        )
        every { getArticleDetailUseCase.invoke(1L) } returns flowOf(
            Result.Loading,
            Result.Success(article)
        )

        val viewModel = ArticleDetailViewModel(
            getArticleDetailUseCase = getArticleDetailUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase
        )

        viewModel.detailUiState.test {
            // Start collecting so `stateIn(WhileSubscribed)` begins collecting upstream.
            viewModel.setArticleId(1L)

            var ui: UiState<Article> = awaitItem()
            // Skip over any Loading emissions (they can be conflated depending on timing).
            while (ui is UiState.Loading) {
                ui = awaitItem()
            }

            assertEquals(UiState.Success(article), ui)
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) { getArticleDetailUseCase.invoke(1L) }
    }

    @Test
    fun `onToggleFavorite invokes usecase with toggled favorite when usecase throws`() = runTest {
        coEvery { toggleFavoriteUseCase.invoke(1L, true) } throws RuntimeException("boom")

        val viewModel = ArticleDetailViewModel(
            getArticleDetailUseCase = getArticleDetailUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase
        )

        val article = Article(
            id = 1L,
            title = "t",
            summary = "s",
            url = "https://example.com/1",
            imageUrl = null,
            newsSite = "X",
            publishedAt = "2026-01-01T00:00:00Z",
            isFavorite = false
        )

        viewModel.onToggleFavorite(article)
        advanceUntilIdle()

        // The ViewModel swallows the exception internally and should not crash the app.
        coVerify(exactly = 1) { toggleFavoriteUseCase.invoke(1L, true) }
    }
}

