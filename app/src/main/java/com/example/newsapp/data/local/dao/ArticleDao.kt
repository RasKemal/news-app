package com.example.newsapp.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.newsapp.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Query(
        """
        SELECT articles.* FROM articles
        INNER JOIN article_remote_keys ON articles.id = article_remote_keys.articleId
        WHERE article_remote_keys.searchQuery = :search
        ORDER BY articles.publishedAt DESC
        """
    )
    fun searchArticles(search: String): PagingSource<Int, ArticleEntity>

    @Query(
        """
        SELECT * FROM articles
        WHERE isFavorite = 1
        ORDER BY publishedAt DESC
        """
    )
    fun getFavoriteArticles(): PagingSource<Int, ArticleEntity>

    @Query(
        """
        SELECT articles.* FROM articles
        JOIN ArticleSearchFtsEntity ON articles.id = ArticleSearchFtsEntity.rowid
        WHERE articles.isFavorite = 1 
        AND ArticleSearchFtsEntity MATCH :search
        ORDER BY articles.publishedAt DESC
        """
    )
    fun searchFavoriteArticles(search: String): PagingSource<Int, ArticleEntity>

    @Query("""
        DELETE FROM articles 
        WHERE isFavorite = 0 
        AND id NOT IN (SELECT articleId FROM article_remote_keys)
        """)
    suspend fun clearOrphanedNonFavoriteArticles()

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    fun observeArticleById(id: Long): Flow<ArticleEntity?>

    @Query("SELECT * FROM articles WHERE id IN (:ids)")
    suspend fun getArticlesByIds(ids: List<Long>): List<ArticleEntity>

    @Query("UPDATE articles SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<ArticleEntity>)

    @Query("DELETE FROM articles")
    suspend fun clearAll()
}

