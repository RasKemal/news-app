package com.example.newsapp.ui.viewmodel

import androidx.paging.PagingData
import app.cash.turbine.test
import com.example.newsapp.core.helpers.UiText
import com.example.newsapp.domain.model.ArticleListLayout
import com.example.newsapp.domain.model.UserPreferences
import com.example.newsapp.domain.usecase.GetArticlesUseCase
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
class AllArticlesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getArticlesUseCase: GetArticlesUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getArticlesUseCase = mockk()
        toggleFavoriteUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onSearchQueryChanged triggers getArticlesUseCase with cleaned query`() = runTest {
        every { getArticlesUseCase(null) } returns flowOf(PagingData.empty())
        every { getArticlesUseCase("mars") } returns flowOf(PagingData.empty())

        val viewModel = AllArticlesViewModel(getArticlesUseCase, toggleFavoriteUseCase)

        viewModel.articles.test {
            awaitItem() // Initial load
            viewModel.onSearchQueryChanged("  mars  ")
            advanceTimeBy(350) // Debounce
            advanceUntilIdle()

            verify(exactly = 1) { getArticlesUseCase("mars") }
            cancelAndIgnoreRemainingEvents()
        }
    }
}

