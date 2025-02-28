package com.example.h2omanager

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.h2omanager.databinding.ActivityAddCustomerBinding
import com.example.h2omanager.databinding.ActivityUserProfileBinding
import com.google.firebase.auth.FirebaseAuth

class AddCustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCustomerBinding
    private lateinit var db: CustomerDatabaseHelper
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = CustomerDatabaseHelper(this)


        binding.saveBtn.setOnClickListener{

            val name = binding.nameEdt.text.toString()
            val number = binding.numEdt.text.toString()

            if (number.isEmpty() || !number.all { it.isDigit() }) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }else if(db.isNumberExists(number)){
                Toast.makeText(this, "This number already exists", Toast.LENGTH_SHORT).show()
            }
            else {
                val customer = Customer(0, name, number)

                val userId = auth.currentUser?.uid

                if (userId != null) {
                    // Sync customer data from Firebase
                    db.insertContact(customer,userId)
                    finish()
                }

                Toast.makeText(this, "Contact Saved", Toast.LENGTH_SHORT).show()
            }
        }
    }
}