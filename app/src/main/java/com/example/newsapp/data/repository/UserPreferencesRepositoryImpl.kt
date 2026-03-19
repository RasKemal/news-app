package com.example.newsapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.newsapp.domain.model.ArticleListLayout
import com.example.newsapp.domain.model.UserPreferences
import com.example.newsapp.domain.repository.UserPreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val ARTICLE_LIST_LAYOUT_KEY = stringPreferencesKey("article_list_layout")

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    override fun observePreferences(): Flow<UserPreferences> =
        dataStore.data.map { prefs ->
            val raw = prefs[ARTICLE_LIST_LAYOUT_KEY] ?: ArticleListLayout.LIST.name
            val layout = runCatching { ArticleListLayout.valueOf(raw) }.getOrDefault(ArticleListLayout.LIST)
            UserPreferences(articleListLayout = layout)
        }

    override suspend fun setArticleListLayout(layout: ArticleListLayout) {
        dataStore.edit { prefs ->
            prefs[ARTICLE_LIST_LAYOUT_KEY] = layout.name
        }
    }
}

