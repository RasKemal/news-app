package com.example.newsapp.domain.model

enum class ArticleListLayout {
    LIST,
    GRID
}

data class UserPreferences(
    val articleListLayout: ArticleListLayout = ArticleListLayout.LIST
)

