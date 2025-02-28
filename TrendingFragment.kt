package com.example.newsaggregatorapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsaggregatorapp.databinding.FragmentTrendingBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TrendingFragment : Fragment() {
    private var _binding: FragmentTrendingBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrendingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchTrendingNews()
    }

    private fun setupRecyclerView() {
        adapter = NewsAdapter(
            onItemClicked = { article ->
                val intent = Intent(requireContext(), ArticleDetailActivity::class.java).apply {
                    putExtra("title", article.title)
                    putExtra("description", article.description)
                    putExtra("imageUrl", article.image)
                    putExtra("publishedAt", article.publishedAt)
                    putExtra("url", article.url)
                }
                startActivity(intent)
            },
            onFavorite = { article ->
                val bookmarked = BookmarkedArticle(
                    title = article.title ?: "",
                    description = article.description,
                    url = article.url ?: "",
                    urlToImage = article.image,
                    publishedAt = article.publishedAt ?: "",
                    isFavorite = true
                )
                saveBookmark(bookmarked)
            },
            onMarkRead = { article ->
                val prefs = requireContext().getSharedPreferences("NewsPrefs", Context.MODE_PRIVATE)
                val readArticles = prefs.getStringSet("readArticles", mutableSetOf()) ?: mutableSetOf()
                readArticles.add(article.url ?: "")
                prefs.edit().putStringSet("readArticles", readArticles).apply()
            }
        )

        binding.rvTrending.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TrendingFragment.adapter
        }
    }

    private fun saveBookmark(article: BookmarkedArticle) {
        val prefs = requireContext().getSharedPreferences("NewsPrefs", Context.MODE_PRIVATE)
        val bookmarksJson = prefs.getString("bookmarks", null)
        val bookmarks = if (bookmarksJson != null) {
            val type = object : TypeToken<MutableSet<BookmarkedArticle>>() {}.type
            Gson().fromJson<MutableSet<BookmarkedArticle>>(bookmarksJson, type)
        } else {
            mutableSetOf()
        }
        bookmarks.add(article)
        prefs.edit().putString("bookmarks", Gson().toJson(bookmarks)).apply()
        Toast.makeText(context, "Article bookmarked", Toast.LENGTH_SHORT).show()
    }

    private fun fetchTrendingNews() {
        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(NewsAPI::class.java)
        api.getTopHeadlines(
            lang = "en",
            token = Constants.API_KEY
        ).enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val articles = response.body()!!.articles
                    adapter.setNews(articles)
                } else {
                    Log.e("TrendingFragment", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(context, "Failed to fetch trending news", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                Log.e("TrendingFragment", "Network error: ${t.message}")
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 