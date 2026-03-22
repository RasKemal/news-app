package com.example.newsapp.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "article_remote_keys",
    primaryKeys = ["articleId", "searchQuery"]
)
data class ArticleRemoteKeysEntity(
    val articleId: Long,
    val searchQuery: String,
    val prevOffset: Int?,
    val nextOffset: Int?
)

