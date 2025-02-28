package com.example.h2omanager




import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.h2omanager.AuthViewModel
import com.example.h2omanager.databinding.ActivityUserProfileBinding

//import kotlinx.android.synthetic.main.activity_user_profile.*

class UserProfileActivity : AppCompatActivity() {
    private val viewModel = AuthViewModel()
    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var customerDatabaseHelper: CustomerDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        customerDatabaseHelper = CustomerDatabaseHelper(this)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("USER_PREF", MODE_PRIVATE)
        binding.name.text = prefs.getString("name", "N/A")
        binding.userName.text = prefs.getString("username", "N/A")
        binding.number.text = prefs.getString("number", "N/A")

        binding.logoutBtn.setOnClickListener {
            viewModel.logout(this)
            customerDatabaseHelper.clearDatabase()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}