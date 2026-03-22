package com.example.newsapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.newsapp.data.local.entity.SearchMetadataEntity

@Dao
interface SearchMetadataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: SearchMetadataEntity)

    @Query("SELECT lastRefreshTime FROM search_metadata WHERE searchQuery = :query")
    suspend fun getLastRefreshTime(query: String): Long?
}