package com.example.newsaggregatorapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {
    @GET("top-headlines")
    fun getTopHeadlines(
        @Query("lang") lang: String = "en",
        @Query("token") token: String
    ): Call<NewsResponse>

    @GET("top-headlines")
    fun getCategoryNews(
        @Query("topic") category: String,
        @Query("lang") lang: String = "en",
        @Query("token") token: String
    ): Call<NewsResponse>

    @GET("search")
    fun searchNews(
        @Query("q") q: String,
        @Query("lang") lang: String = "en",
        @Query("token") token: String
    ): Call<NewsResponse>

    @GET("top-headlines")
    fun getNewsBySource(
        @Query("sources") sources: String,
        @Query("apiKey") apiKey: String
    ): Call<NewsResponse>

    @GET("everything")
    fun getTrendingNews(
        @Query("sortBy") sortBy: String,
        @Query("language") language: String,
        @Query("apiKey") apiKey: String
    ): Call<NewsResponse>
}
