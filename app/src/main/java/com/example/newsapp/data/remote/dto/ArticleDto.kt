package com.example.newsapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NewsResponse(
    val count: Int?,
    val next: String?,
    val previous: String?,
    val results: List<ArticleDto>
)

data class ArticleDto(
    val id: Long,
    val title: String,
    val summary: String,
    val url: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("news_site") val newsSite: String?,
    @SerializedName("published_at") val publishedAt: String?
)

