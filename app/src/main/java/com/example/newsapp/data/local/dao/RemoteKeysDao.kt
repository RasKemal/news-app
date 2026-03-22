package com.example.newsapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.newsapp.data.local.entity.ArticleRemoteKeysEntity

@Dao
interface RemoteKeysDao {

    @Query(
        """
        SELECT * FROM article_remote_keys
        WHERE articleId = :articleId AND searchQuery = :search
        """
    )
    suspend fun remoteKeysByArticleId(articleId: Long, search: String): ArticleRemoteKeysEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(keys: List<ArticleRemoteKeysEntity>)

    @Query("DELETE FROM article_remote_keys WHERE searchQuery = :search")
    suspend fun clearRemoteKeysForQuery(search: String)

    @Query("SELECT EXISTS(SELECT 1 FROM article_remote_keys WHERE searchQuery = :search)")
    suspend fun hasRemoteKeysForQuery(search: String): Boolean
}

