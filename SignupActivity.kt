package com.example.newsaggregatorapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.newsaggregatorapp.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Sign Up button functionality
        binding.btnSignup.setOnClickListener {
            registerUser()
        }

        // Login text functionality
        binding.tvLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun registerUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.error = "Email is required."
            return
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.error = "Password is required."
            return
        }

        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters long."
            return
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match."
            return
        }

        // Register the user in Firebase
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
