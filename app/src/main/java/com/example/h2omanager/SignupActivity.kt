package com.example.h2omanager

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.h2omanager.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {
    private val viewModel = AuthViewModel()
    private lateinit var binding: ActivitySignupBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseAuth.getInstance().setLanguageCode("en")

        binding.AlreadyHave.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }


        binding.signup.setOnClickListener {
            val name = binding.signupName.text.toString()
            val username = binding.signupUserName.text.toString()
            val number = binding.signupNumber.text.toString()
            val password = binding.signupPassword.text.toString()

            if (name.isEmpty() || username.isEmpty() || number.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            viewModel.signUp(name, username, number, password) { success, message ->
                if (success) {

                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}