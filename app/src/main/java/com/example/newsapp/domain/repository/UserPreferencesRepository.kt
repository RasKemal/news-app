package com.example.newsapp.domain.repository

import com.example.newsapp.domain.model.ArticleListLayout
import com.example.newsapp.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun observePreferences(): Flow<UserPreferences>
    suspend fun setArticleListLayout(layout: ArticleListLayout)
}

