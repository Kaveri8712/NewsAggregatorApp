package com.example.newsaggregatorapp


data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)