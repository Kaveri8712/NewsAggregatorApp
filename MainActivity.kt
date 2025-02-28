package com.example.newsaggregatorapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var welcomeMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)
        welcomeMessage = findViewById(R.id.tvWelcomeMessage)

        progressBar.visibility = View.VISIBLE

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            startActivity(Intent(this, NewsActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }
}

