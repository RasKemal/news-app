package com.example.newsapp.domain.model

data class Article(
    val id: Long,
    val title: String,
    val summary: String,
    val url: String,
    val imageUrl: String?,
    val newsSite: String?,
    val publishedAt: String?,
    val isFavorite: Boolean
)

