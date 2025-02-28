package com.example.newsaggregatorapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_articles")
data class OfflineArticle(
    @PrimaryKey
    val url: String,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val publishedAt: String,
    val content: String,
    val downloadDate: Long = System.currentTimeMillis()
) 