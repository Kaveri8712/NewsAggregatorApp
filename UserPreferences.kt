package com.example.newsaggregatorapp

data class UserPreferences(
    val favoriteSources: MutableSet<String> = mutableSetOf(),
    val blockedTopics: MutableSet<String> = mutableSetOf(),
    val preferredCategories: MutableSet<String> = mutableSetOf()
) 