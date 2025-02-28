package com.example.newsaggregatorapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsaggregatorapp.databinding.FragmentBookmarksBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BookmarksFragment : Fragment() {
    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!
    private lateinit var bookmarksAdapter: BookmarksAdapter

    override fun onResume() {
        super.onResume()
        loadBookmarks()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        setupRecyclerView()
        loadBookmarks()
        return binding.root
    }

    private fun setupRecyclerView() {
        bookmarksAdapter = BookmarksAdapter(
            onItemClicked = { article ->
                val intent = Intent(requireContext(), ArticleDetailActivity::class.java).apply {
                    putExtra("title", article.title)
                    putExtra("description", article.description)
                    putExtra("imageUrl", article.urlToImage)
                    putExtra("publishedAt", article.publishedAt)
                    putExtra("url", article.url)
                }
                startActivity(intent)
            },
            onRemoveBookmark = { article ->
                removeBookmark(article)
            }
        )
        
        binding.rvBookmarks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bookmarksAdapter
        }
    }

    private fun loadBookmarks() {
        val prefs = requireContext().getSharedPreferences("NewsPrefs", Context.MODE_PRIVATE)
        val bookmarksJson = prefs.getString("bookmarks", null)
        if (bookmarksJson != null) {
            val type = object : TypeToken<Set<BookmarkedArticle>>() {}.type
            val bookmarks: Set<BookmarkedArticle> = Gson().fromJson(bookmarksJson, type)
            if (bookmarks.isEmpty()) {
                binding.tvNoBookmarks.visibility = View.VISIBLE
                binding.rvBookmarks.visibility = View.GONE
            } else {
                binding.tvNoBookmarks.visibility = View.GONE
                binding.rvBookmarks.visibility = View.VISIBLE
                bookmarksAdapter.setBookmarks(bookmarks.toList())
            }
        } else {
            binding.tvNoBookmarks.visibility = View.VISIBLE
            binding.rvBookmarks.visibility = View.GONE
        }
    }

    private fun removeBookmark(article: BookmarkedArticle) {
        val prefs = requireContext().getSharedPreferences("NewsPrefs", Context.MODE_PRIVATE)
        val bookmarksJson = prefs.getString("bookmarks", null)
        if (bookmarksJson != null) {
            val type = object : TypeToken<MutableSet<BookmarkedArticle>>() {}.type
            val bookmarks: MutableSet<BookmarkedArticle> = Gson().fromJson(bookmarksJson, type)
            bookmarks.removeIf { it.url == article.url }
            prefs.edit().putString("bookmarks", Gson().toJson(bookmarks)).apply()
            loadBookmarks()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 