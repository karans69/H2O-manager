package com.example.h2omanager


import android.app.Activity
import android.graphics.Paint
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.h2omanager.databinding.ActivityAddPaymentBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.Date
import java.util.Locale

class AddPaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPaymentBinding
    private lateinit var customerDatabaseHelper: CustomerDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customerDatabaseHelper = CustomerDatabaseHelper(this)

        // Retrieve customer data passed from the previous activity
        val customerId = intent.getIntExtra("customerId", -1)
        binding.nameTextview.text = intent.getStringExtra("customerName") ?: ""
        binding.numberTextview.text = intent.getStringExtra("customerNumber") ?: ""

        // Set up NumberPicker
        val currentStock = customerDatabaseHelper.getBottleStock()
        initializeNumberPicker(binding.numberPicker, currentStock)

        // Set the current date in editTextDate
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        binding.editTextDate.setText(currentDate)

        // Handle button clicks
        binding.paidBtn.setOnClickListener { handlePayment(customerId, "Paid") }
        binding.advBtn.setOnClickListener { handlePayment(customerId, "Advance") }
        binding.pendingBtn.setOnClickListener { handlePayment(customerId, "Pending") }
    }

    private fun initializeNumberPicker(numberPicker: NumberPicker, currentStock: Int) {
        numberPicker.minValue = 0
        numberPicker.maxValue = currentStock
        numberPicker.value = currentStock
    }


    private fun validateTransactionInput(): Pair<Double, String>? {
        val amountText = binding.paymentInput.text.toString()
        val date = binding.editTextDate.text.toString()

        if (amountText.isEmpty()) {
            Toast.makeText(this, "Please enter a valid amount!", Toast.LENGTH_SHORT).show()
            return null
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Invalid amount!", Toast.LENGTH_SHORT).show()
            return null
        }

        return Pair(amount, date)
    }

    private fun handlePayment(customerId: Int, status: String) {
        val input = validateTransactionInput() ?: return
        val (amount, date) = input

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = Transaction(
            transactionId = 0, // SQLite auto-generates this
            customerId = customerId,
            amount = amount,
            date = date,
            status = status
        )

        customerDatabaseHelper.updateBottleStock(binding.numberPicker.value)
        customerDatabaseHelper.addTransaction(transaction, userId)

        Toast.makeText(this, "Transaction added successfully!", Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK)
        finish()
    }


}