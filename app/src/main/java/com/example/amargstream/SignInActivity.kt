package com.example.amargstream

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amargstream.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class SignInActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signInButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            val emailPattern = Patterns.EMAIL_ADDRESS.pattern()
            when {
                !Pattern.matches(emailPattern, email) -> {
                    binding.emailEditText.error = "Invalid email address!"
                }
                password.length < 6 -> {
                    binding.passwordEditText.error = "Wrong password!"
                }
                else -> {
                    signInWithFirebase(email, password)
                }
            }
        }

        binding.gotoSignUp.setOnClickListener {
            startActivity(Intent(this@SignInActivity, SignUpActivity :: class.java))
        }
    }

    private fun signInWithFirebase(email: String, password: String) {
        setInProgress(true)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                setInProgress(false)
                startActivity(Intent(this@SignInActivity, MainActivity :: class.java))
                finish()
            }.addOnFailureListener {
                setInProgress(false)
                Toast.makeText(applicationContext, "User authentication failed.", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    override fun onResume() {
        super.onResume()
        FirebaseAuth.getInstance().currentUser?.apply {
            startActivity(Intent(this@SignInActivity, MainActivity :: class.java))
            finish()
        }
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.signInButton.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.signInButton.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }
}