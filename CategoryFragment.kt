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
import com.example.newsaggregatorapp.databinding.FragmentCategoryBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CategoryFragment : Fragment() {
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NewsAdapter
    private lateinit var category: String
    private val API_KEY = "bd9303a2aef9401918666c411978409a"

    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: String): CategoryFragment {
            return CategoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY, category)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = arguments?.getString(ARG_CATEGORY) ?: "general"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchCategoryNews()
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
                val prefs = requireContext().getSharedPreferences("NewsPrefs", Context.MODE_PRIVATE)
                val bookmarked = BookmarkedArticle(
                    title = article.title ?: "",
                    description = article.description,
                    url = article.url ?: "",
                    urlToImage = article.image,
                    publishedAt = article.publishedAt ?: "",
                    isFavorite = true
                )
                val bookmarksJson = prefs.getString("bookmarks", null)
                val bookmarkedArticles = if (bookmarksJson != null) {
                    val type = object : TypeToken<Set<BookmarkedArticle>>() {}.type
                    Gson().fromJson<Set<BookmarkedArticle>>(bookmarksJson, type).toMutableSet()
                } else {
                    mutableSetOf()
                }
                bookmarkedArticles.add(bookmarked)
                prefs.edit().putString("bookmarks", Gson().toJson(bookmarkedArticles)).apply()
                Toast.makeText(context, "Article bookmarked", Toast.LENGTH_SHORT).show()
            },
            onMarkRead = { article ->
                val prefs = requireContext().getSharedPreferences("NewsPrefs", Context.MODE_PRIVATE)
                val readArticles = prefs.getStringSet("readArticles", mutableSetOf()) ?: mutableSetOf()
                readArticles.add(article.url ?: "")
                prefs.edit().putStringSet("readArticles", readArticles).apply()
                Toast.makeText(context, "Article marked as read", Toast.LENGTH_SHORT).show()
            }
        )
        binding.categoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CategoryFragment.adapter
        }
    }

    private fun fetchCategoryNews() {
        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(NewsAPI::class.java)
        
        binding.progressBar.visibility = View.VISIBLE
        
        api.getCategoryNews(
            category = category.lowercase(),
            lang = "en",
            token = Constants.API_KEY
        ).enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val articles = response.body()!!.articles
                    if (articles.isNotEmpty()) {
                        adapter.setNews(articles)
                    } else {
                        showError("No news found for this category")
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        429 -> "Too many requests. Please try again later."
                        401 -> "Invalid API key. Please check your configuration."
                        else -> "Failed to fetch news. Error: ${response.code()}"
                    }
                    showError(errorMsg)
                    Log.e("CategoryFragment", "Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                showError("Network error: ${t.localizedMessage}")
                Log.e("CategoryFragment", "Error fetching news", t)
            }
        })
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun updateArticles(articles: List<Article>) {
        adapter.setNews(articles)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 