package com.example.newsapp.ui.viewmodel

import androidx.paging.PagingData
import app.cash.turbine.test
import com.example.newsapp.core.helpers.UiText
import com.example.newsapp.domain.model.ArticleListLayout
import com.example.newsapp.domain.model.UserPreferences
import com.example.newsapp.domain.usecase.GetFavoriteArticlesUseCase
import com.example.newsapp.domain.usecase.ObserveUserPreferencesUseCase
import com.example.newsapp.domain.usecase.SetArticleListLayoutUseCase
import com.example.newsapp.domain.usecase.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.lang.RuntimeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getFavoriteArticlesUseCase: GetFavoriteArticlesUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getFavoriteArticlesUseCase = mockk()
        toggleFavoriteUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onSearchQueryChanged triggers getFavoriteArticlesUseCase with cleaned query`() = runTest {
        every { getFavoriteArticlesUseCase(null) } returns flowOf(PagingData.empty())
        every { getFavoriteArticlesUseCase("jupiter") } returns flowOf(PagingData.empty())

        val viewModel = FavoritesViewModel(getFavoriteArticlesUseCase, toggleFavoriteUseCase)

        viewModel.articles.test {
            awaitItem()
            viewModel.onSearchQueryChanged("  jupiter  ")
            advanceTimeBy(350)
            advanceUntilIdle()

            verify(exactly = 1) { getFavoriteArticlesUseCase("jupiter") }
            cancelAndIgnoreRemainingEvents()
        }
    }
}

