package com.example.newsapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val summary: String,
    val url: String,
    val imageUrl: String?,
    val newsSite: String?,
    val publishedAt: String?,
    val isFavorite: Boolean = false
)

