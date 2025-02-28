package com.example.newsaggregatorapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: OfflineArticle)

    @Query("SELECT * FROM offline_articles ORDER BY downloadDate DESC")
    fun getAllArticles(): Flow<List<OfflineArticle>>

    @Query("SELECT * FROM offline_articles WHERE url = :url")
    suspend fun getArticleByUrl(url: String): OfflineArticle?

    @Delete
    suspend fun deleteArticle(article: OfflineArticle)

    @Query("DELETE FROM offline_articles")
    suspend fun deleteAllArticles()
} 