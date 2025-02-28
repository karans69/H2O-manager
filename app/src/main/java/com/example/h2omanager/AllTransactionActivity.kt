package com.example.h2omanager

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.h2omanager.databinding.ActivityAllTransactionBinding
import java.io.File
import java.io.FileWriter
import java.util.Locale

class AllTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllTransactionBinding
    private lateinit var db: CustomerDatabaseHelper
    private var filteredDate: String? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityAllTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = CustomerDatabaseHelper(this)

        // Setup RecyclerView
        setupRecyclerView()


        // Date filter
        binding.dateFilter.setOnClickListener {
            showDatePicker { selectedDate ->
                filteredDate = selectedDate
                setupRecyclerView() // Reload data with filter
            }
        }

        // Download button
        binding.downloadBtn.setOnClickListener {
            downloadTransactions()
        }
    }
    private fun setupRecyclerView() {
        val transactions = if (filteredDate == null) {
            db.getAllTransactionsWithCustomer() // Fetch all transactions with customer details
        } else {
            db.getTransactionsWithCustomerByDate(filteredDate!!) // Fetch filtered transactions
        }

        val sortedTransactions = transactions.sortedByDescending { it.date }

        val adapter = AllTransactionAdapter(sortedTransactions)
        binding.transactionRecyclerView.adapter = adapter
        binding.transactionRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun downloadTransactions() {
        val transactions = if (filteredDate == null) {
            db.getAllTransactionsWithCustomer()
        } else {
            db.getTransactionsWithCustomerByDate(filteredDate!!)
        }

        if (transactions.isEmpty()) {
            Toast.makeText(this, "No transactions to download", Toast.LENGTH_SHORT).show()
            return
        }

        val csvContent = buildString {
            append("Customer Name,Customer Number,Amount,Date\n")
            transactions.forEach { transaction ->
                append("${transaction.customerName},${transaction.customerNumber},${transaction.amount},${transaction.date}\n")
            }
        }

        try {
            val fileName = if (filteredDate == null) "all_transactions.csv" else "transactions_$filteredDate.csv"
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            FileWriter(file).use { writer ->
                writer.write(csvContent)
            }

            Toast.makeText(this, "File saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}