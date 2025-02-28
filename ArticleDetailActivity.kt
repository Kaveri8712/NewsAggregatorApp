package com.example.newsaggregatorapp

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.newsaggregatorapp.databinding.ActivityArticleDetailBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ArticleDetailActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityArticleDetailBinding
    private var isBookmarked = false
    private lateinit var tts: TextToSpeech
    private var isSpeaking = false
    private var currentLanguage = Locale.ENGLISH
    private var currentText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tts = TextToSpeech(this, this)
        setupLanguageSpinner()
        setupTTSControls()

        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description")
        val imageUrl = intent.getStringExtra("imageUrl")
        val publishedAt = intent.getStringExtra("publishedAt") ?: ""
        val url = intent.getStringExtra("url") ?: ""

        // Check if article is already bookmarked
        isBookmarked = checkIfBookmarked(url)
        updateBookmarkIcon()

        binding.apply {
            tvTitle.text = title
            tvDescription.text = description
            tvPublishedAt.text = publishedAt

            imageUrl?.let { url ->
                Glide.with(this@ArticleDetailActivity)
                    .load(url)
                    .into(ivArticleImage)
            }

            btnBookmark.setOnClickListener {
                isBookmarked = !isBookmarked
                updateBookmarkIcon()
                if (isBookmarked) {
                    bookmarkArticle(title, description, url, imageUrl, publishedAt)
                } else {
                    removeBookmark(url)
                }
            }
        }

        // Get intent extras and setup UI
        intent.extras?.let { bundle ->
            val titleFromBundle = bundle.getString("title", "")
            val descriptionFromBundle = bundle.getString("description", "")
            currentText = "$titleFromBundle\n\n$descriptionFromBundle"
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = currentLanguage
        } else {
            Toast.makeText(this, "TTS Initialization failed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLanguageSpinner() {
        val languages = mapOf(
            "English" to Locale.ENGLISH,
            "French" to Locale.FRENCH,
            "German" to Locale.GERMAN,
            "Italian" to Locale.ITALIAN,
            "Spanish" to Locale("es")
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages.keys.toList()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = adapter

        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = languages[languages.keys.toList()[position]]
                if (selectedLanguage != null) {
                    currentLanguage = selectedLanguage
                    tts.language = currentLanguage
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupTTSControls() {
        binding.btnPlayPause.setOnClickListener {
            if (isSpeaking) {
                stopSpeech()
            } else {
                playSpeech()
            }
        }

        binding.btnStop.setOnClickListener {
            stopSpeech()
        }
    }

    private fun playSpeech() {
        if (!isSpeaking) {
            val result = tts.speak(currentText, TextToSpeech.QUEUE_FLUSH, null, null)
            if (result == TextToSpeech.SUCCESS) {
                isSpeaking = true
                updatePlayPauseButton()
            }
        }
    }

    private fun stopSpeech() {
        if (isSpeaking) {
            tts.stop()
            isSpeaking = false
            updatePlayPauseButton()
        }
    }

    private fun updatePlayPauseButton() {
        binding.btnPlayPause.setImageResource(
            if (isSpeaking) R.drawable.ic_pause
            else R.drawable.ic_play
        )
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    private fun updateBookmarkIcon() {
        binding.btnBookmark.setImageResource(
            if (isBookmarked) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
    }

    private fun checkIfBookmarked(url: String): Boolean {
        val prefs = getSharedPreferences("NewsPrefs", MODE_PRIVATE)
        val bookmarksJson = prefs.getString("bookmarks", null)
        if (bookmarksJson != null) {
            val type = object : TypeToken<Set<BookmarkedArticle>>() {}.type
            val bookmarks: Set<BookmarkedArticle> = Gson().fromJson(bookmarksJson, type)
            return bookmarks.any { it.url == url }
        }
        return false
    }

    private fun bookmarkArticle(title: String, description: String?, url: String, imageUrl: String?, publishedAt: String) {
        val prefs = getSharedPreferences("NewsPrefs", MODE_PRIVATE)
        val bookmarksJson = prefs.getString("bookmarks", null)
        val bookmarks = if (bookmarksJson != null) {
            val type = object : TypeToken<MutableSet<BookmarkedArticle>>() {}.type
            Gson().fromJson<MutableSet<BookmarkedArticle>>(bookmarksJson, type)
        } else {
            mutableSetOf()
        }

        val bookmarked = BookmarkedArticle(
            title = title,
            description = description,
            url = url,
            urlToImage = imageUrl,
            publishedAt = publishedAt,
            isFavorite = true
        )
        bookmarks.add(bookmarked)
        prefs.edit().putString("bookmarks", Gson().toJson(bookmarks)).apply()
        Toast.makeText(this, "Article bookmarked", Toast.LENGTH_SHORT).show()
    }

    private fun removeBookmark(url: String) {
        val prefs = getSharedPreferences("NewsPrefs", MODE_PRIVATE)
        val bookmarksJson = prefs.getString("bookmarks", null)
        if (bookmarksJson != null) {
            val type = object : TypeToken<MutableSet<BookmarkedArticle>>() {}.type
            val bookmarks: MutableSet<BookmarkedArticle> = Gson().fromJson(bookmarksJson, type)
            bookmarks.removeIf { it.url == url }
            prefs.edit().putString("bookmarks", Gson().toJson(bookmarks)).apply()
            Toast.makeText(this, "Bookmark removed", Toast.LENGTH_SHORT).show()
        }
    }
} 