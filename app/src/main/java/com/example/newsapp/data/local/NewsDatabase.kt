package com.example.newsapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.newsapp.data.local.dao.ArticleDao
import com.example.newsapp.data.local.dao.RemoteKeysDao
import com.example.newsapp.data.local.dao.SearchMetadataDao
import com.example.newsapp.data.local.entity.ArticleEntity
import com.example.newsapp.data.local.entity.ArticleRemoteKeys
import com.example.newsapp.data.local.entity.ArticleSearchFtsEntity
import com.example.newsapp.data.local.entity.SearchMetadataEntity

@Database(
    entities = [
        ArticleEntity::class,
        ArticleRemoteKeys::class,
        ArticleSearchFtsEntity::class,
        SearchMetadataEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun remoteKeysDao(): RemoteKeysDao
    abstract fun searchMetadataDao(): SearchMetadataDao
}

