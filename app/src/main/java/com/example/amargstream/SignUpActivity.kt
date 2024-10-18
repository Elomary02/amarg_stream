package com.example.amargstream

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amargstream.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createAccountButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            val emailPattern = Patterns.EMAIL_ADDRESS.pattern()
            when {
                !Pattern.matches(emailPattern, email) -> {
                    binding.emailEditText.error = "Invalid email address!"
                }
                password.length < 6 -> {
                    binding.passwordEditText.error = "Weak password, try to make it longer!"
                }
                password != confirmPassword -> {
                    binding.confirmPasswordEditText.error = "Unmatched password"
                }
                else -> {
                    createAccountWithFirebase(email, password)
                }
            }
        }

        binding.gotoSignIn.setOnClickListener {
            finish()
        }
    }

    private fun createAccountWithFirebase(email: String, password: String) {
        setInProgress(true)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                setInProgress(false)
                startActivity(Intent(this@SignUpActivity, SignInActivity :: class.java))
            }.addOnFailureListener {
                setInProgress(false)
                Toast.makeText(applicationContext, "User creation failed.", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.createAccountButton.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.createAccountButton.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }
}