package com.example.newsapp.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

/**
 * Room-managed FTS4 virtual table for fast offline keyword search.
 *
 * Notes:
 * - Room 2.7 + KSP requires both [@Entity] and [@Fts4] on the FTS class.
 * - We use a `rowid` primary key field (as required by Room FTS entity rules).
 * - Because `ArticleEntity.id` is an INTEGER PRIMARY KEY in SQLite, it can be used as the FTS rowid.
 */
@Entity
@Fts4(contentEntity = ArticleEntity::class)
data class ArticleSearchFtsEntity(
    @PrimaryKey val rowid: Long,
    val title: String,
    val summary: String
)

