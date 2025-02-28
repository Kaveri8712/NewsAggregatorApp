package com.example.newsaggregatorapp

val bookmarked = BookmarkedArticle(
    title = article.title ?: "",
    description = article.description,
    url = article.url ?: "",
    urlToImage = article.image,  // Changed from urlToImage to image
    publishedAt = article.publishedAt ?: "",
    isFavorite = true
)