package com.example.newsaggregatorapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsaggregatorapp.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sourcesAdapter: SourcesAdapter
    private lateinit var preferences: SharedPreferences
    private val availableSources = listOf(
        "bbc-news",
        "cnn",
        "reuters",
        "associated-press",
        "bloomberg",
        "the-verge",
        "techcrunch",
        "the-wall-street-journal"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        preferences = requireContext().getSharedPreferences("NewsPrefs", Context.MODE_PRIVATE)
        setupRecyclerViews()
        loadPreferences()
        return binding.root
    }

    private fun setupRecyclerViews() {
        sourcesAdapter = SourcesAdapter { source, isSelected ->
            val sources = preferences.getStringSet("favoriteSources", mutableSetOf()) ?: mutableSetOf()
            if (isSelected) sources.add(source) else sources.remove(source)
            preferences.edit().putStringSet("favoriteSources", sources).apply()
        }
        binding.rvSources.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sourcesAdapter
        }
    }

    private fun loadPreferences() {
        val savedSources = preferences.getStringSet("favoriteSources", mutableSetOf()) ?: mutableSetOf()
        sourcesAdapter.updateSources(availableSources)
    }
} 