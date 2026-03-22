package com.example.newsapp.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Entity
@Fts4(contentEntity = ArticleEntity::class)
data class ArticleSearchFtsEntity(
    @PrimaryKey val rowid: Long,
    val title: String,
    val summary: String
)

