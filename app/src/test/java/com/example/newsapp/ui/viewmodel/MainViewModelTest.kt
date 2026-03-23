package com.example.newsapp.ui.viewmodel

import app.cash.turbine.test
import com.example.newsapp.R
import com.example.newsapp.core.helpers.UiText
import com.example.newsapp.domain.model.ArticleListLayout
import com.example.newsapp.domain.model.UserPreferences
import com.example.newsapp.domain.usecase.ObserveUserPreferencesUseCase
import com.example.newsapp.domain.usecase.SetArticleListLayoutUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var observeUserPreferencesUseCase: ObserveUserPreferencesUseCase
    private lateinit var setArticleListLayoutUseCase: SetArticleListLayoutUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        observeUserPreferencesUseCase = mockk()
        setArticleListLayoutUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `userPreferences emits initial then values from usecase`() = runTest {
        val expectedPrefs = UserPreferences(articleListLayout = ArticleListLayout.GRID)
        every { observeUserPreferencesUseCase() } returns flowOf(expectedPrefs)

        val viewModel = MainViewModel(observeUserPreferencesUseCase, setArticleListLayoutUseCase)

        viewModel.userPreferences.test {
            // StateFlow emits initialValue first
            assertEquals(UserPreferences(), awaitItem())
            // Then emits value from usecase
            assertEquals(expectedPrefs, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setLayout emits error message to snackbar on failure`() = runTest {
        every { observeUserPreferencesUseCase() } returns flowOf(UserPreferences())
        coEvery { setArticleListLayoutUseCase(any()) } throws RuntimeException("DataStore Error")

        val viewModel = MainViewModel(observeUserPreferencesUseCase, setArticleListLayoutUseCase)

        viewModel.snackbarMessages.test {
            viewModel.setLayout(ArticleListLayout.GRID)
            advanceUntilIdle()

            val error = awaitItem()
            assert(error is UiText.DynamicString)
            assertEquals("DataStore Error", (error as UiText.DynamicString).value)
        }
    }
}