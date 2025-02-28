package com.example.h2omanager

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.h2omanager.databinding.ActivityUpdateBinding
import com.google.firebase.auth.FirebaseAuth

class UpdateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateBinding
    private lateinit var db: CustomerDatabaseHelper
    private  var customerId : Int = -1
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)



        db = CustomerDatabaseHelper(this)



        customerId = intent.getIntExtra("customer_id",-1)
        if(customerId == -1){
            finish()
            return
        }
        val customer = db.getCustomerById(customerId)
        if (customer != null) {
            binding.UpdateNameEdt.setText(customer.name)
            binding.UpdateNumberEdt.setText(customer.number)
        }

        binding.updateSaveBtn.setOnClickListener{
            val newName = binding.UpdateNameEdt.text.toString()
            val newNumber = binding.UpdateNumberEdt.text.toString()
            val updateDetails = Customer(customerId,newName,newNumber)

            val userId = auth.currentUser?.uid

            if (userId != null) {
                db.updateCustomer(updateDetails,userId)
                finish()
            }

            Toast.makeText(this,"Changes Saved", Toast.LENGTH_SHORT).show()
        }
    }
}