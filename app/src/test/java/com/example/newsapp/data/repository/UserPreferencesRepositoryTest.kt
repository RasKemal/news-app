package com.example.newsapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.newsapp.domain.model.ArticleListLayout
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UserPreferencesRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var dataStoreFile: File

    private fun createRepository(testScope: TestScope): UserPreferencesRepositoryImpl {
        dataStoreFile = File(context.filesDir, "test-user-preferences.preferences_pb")
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            corruptionHandler = null,
            migrations = emptyList(),
            scope = testScope.backgroundScope,
            produceFile = { dataStoreFile }
        )
        return UserPreferencesRepositoryImpl(dataStore)
    }

    @After
    fun tearDown() {
        dataStoreFile.delete()
    }

    @Test
    fun `observePreferences emits LIST by default when datastore is empty`() = runTest {
        val repository = createRepository(this)
        val preferences = repository.observePreferences().first()
        assertEquals(ArticleListLayout.LIST, preferences.articleListLayout)
    }

    @Test
    fun `setArticleListLayout updates observePreferences flow to GRID`() = runTest {
        val repository = createRepository(this)
        repository.observePreferences().test {
            assertEquals(ArticleListLayout.LIST, awaitItem().articleListLayout)

            repository.setArticleListLayout(ArticleListLayout.GRID)

            assertEquals(ArticleListLayout.GRID, awaitItem().articleListLayout)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
