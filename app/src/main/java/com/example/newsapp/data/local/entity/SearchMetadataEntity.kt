package com.example.newsapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_metadata")
data class SearchMetadataEntity(
    @PrimaryKey val searchQuery: String,
    val lastRefreshTime: Long
)