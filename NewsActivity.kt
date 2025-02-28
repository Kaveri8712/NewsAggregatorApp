package com.example.newsaggregatorapp

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsaggregatorapp.databinding.ActivityNewsBinding
import com.google.firebase.auth.FirebaseAuth
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.newsaggregatorapp.SwipeGestureCallback

class NewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewsBinding
    private lateinit var selectedLanguage: String
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var tts: TextToSpeech
    private val userPreferences by lazy {
        UserPreferences(
            favoriteSources = getSharedPreferences("NewsPrefs", MODE_PRIVATE)
                .getStringSet("favoriteSources", mutableSetOf()) ?: mutableSetOf(),
            blockedTopics = getSharedPreferences("NewsPrefs", MODE_PRIVATE)
                .getStringSet("blockedTopics", mutableSetOf()) ?: mutableSetOf()
        )
    }

    private val retrofit: Retrofit by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val bookmarkedArticles = mutableSetOf<BookmarkedArticle>()
    private val readArticles = mutableSetOf<String>() // Store URLs of read articles

    companion object {
        const val VOICE_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupBottomNavigation()
        loadBookmarks()
        loadReadArticles()
        initializeTextToSpeech()

        // Set default language
        selectedLanguage = "en" // Default to English

        // Initialize search view
        setupSearchView()
        setupVoiceSearch()
        setupLanguageSpinner()

        // Fetch initial news
        fetchNews()
    }

    private fun initializeTextToSpeech() {
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.getDefault()
            }
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    fetchNews(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        binding.searchView.setIconifiedByDefault(false)
    }

    private fun setupViewPager() {
        viewPager = binding.viewPager
        tabLayout = binding.tabLayout

        val categories = listOf("General", "Business", "Technology", "Sports", "Entertainment", "Health", "Science")
        val pagerAdapter = NewsPagerAdapter(this, categories)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = categories[position]
        }.attach()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    binding.searchContainer.visibility = View.VISIBLE
                    binding.tabLayout.visibility = View.VISIBLE
                    binding.viewPager.visibility = View.VISIBLE
                    binding.bookmarksContainer.visibility = View.GONE
                    binding.trendingContainer.visibility = View.GONE
                    true
                }
                R.id.nav_trending -> {
                    binding.searchContainer.visibility = View.GONE
                    binding.tabLayout.visibility = View.GONE
                    binding.viewPager.visibility = View.GONE
                    binding.bookmarksContainer.visibility = View.GONE
                    binding.trendingContainer.visibility = View.VISIBLE
                    
                    if (supportFragmentManager.findFragmentById(R.id.trendingContainer) == null) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.trendingContainer, TrendingFragment())
                            .commit()
                    }
                    true
                }
                R.id.nav_bookmarks -> {
                    binding.searchContainer.visibility = View.GONE
                    binding.tabLayout.visibility = View.GONE
                    binding.viewPager.visibility = View.GONE
                    binding.bookmarksContainer.visibility = View.VISIBLE
                    binding.trendingContainer.visibility = View.GONE
                    
                    if (supportFragmentManager.findFragmentById(R.id.bookmarksContainer) == null) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.bookmarksContainer, BookmarksFragment())
                            .commit()
                    }
                    true
                }
                else -> false
            }
        }

        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun saveBookmarks() {
        val prefs = getSharedPreferences("NewsPrefs", MODE_PRIVATE)
        val bookmarksJson = Gson().toJson(bookmarkedArticles)
        prefs.edit().putString("bookmarks", bookmarksJson).apply()
    }

    private fun loadBookmarks() {
        val prefs = getSharedPreferences("NewsPrefs", MODE_PRIVATE)
        val bookmarksJson = prefs.getString("bookmarks", null)
        if (bookmarksJson != null) {
            val type = object : TypeToken<Set<BookmarkedArticle>>() {}.type
            bookmarkedArticles.addAll(Gson().fromJson(bookmarksJson, type))
        }
    }

    private fun saveReadArticles() {
        val prefs = getSharedPreferences("NewsPrefs", MODE_PRIVATE)
        prefs.edit().putStringSet("readArticles", readArticles).apply()
    }

    private fun loadReadArticles() {
        val prefs = getSharedPreferences("NewsPrefs", MODE_PRIVATE)
        readArticles.addAll(prefs.getStringSet("readArticles", setOf()) ?: setOf())
    }

    private fun fetchNews(query: String? = null) {
        val api = retrofit.create(NewsAPI::class.java)

        val call = if (query.isNullOrEmpty()) {
            api.getTopHeadlines(
                lang = selectedLanguage,
                token = Constants.API_KEY
            )
        } else {
            api.searchNews(
                q = query,
                lang = selectedLanguage,
                token = Constants.API_KEY
            )
        }

        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val articles = response.body()!!.articles
                    val currentFragment = supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}")
                    if (currentFragment is CategoryFragment) {
                        currentFragment.updateArticles(articles)
                    }
                } else {
                    Log.e("NewsActivity", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@NewsActivity, "Failed to fetch news", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                Log.e("NewsActivity", "Network error: ${t.message}")
                Toast.makeText(this@NewsActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupVoiceSearch() {
        val voiceIcon = findViewById<ImageView>(R.id.voiceIcon)
        voiceIcon.setOnClickListener {
            startVoiceRecognition()
        }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now to search for news")
        }

        try {
            startActivityForResult(intent, VOICE_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Voice search is not supported on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val query = results?.get(0)
            if (!query.isNullOrEmpty()) {
                Log.d("NewsActivity", "Voice query: $query")
                binding.searchView.setQuery(query, true)
            }
        }
    }

    private fun setupLanguageSpinner() {
        val languages = resources.getStringArray(R.array.language_options)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = spinnerAdapter

        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedLanguage = when (position) {
                    0 -> "en" // English
                    1 -> "hi" // Hindi
                    2 -> "fr" // French
                    else -> "en"
                }
                fetchNews() // Fetch news based on the selected language
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }
}
