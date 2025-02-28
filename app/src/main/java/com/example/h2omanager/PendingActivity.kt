package com.example.h2omanager

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.h2omanager.databinding.ActivityPendingBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PendingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPendingBinding
    private lateinit var customerDatabaseHelper: CustomerDatabaseHelper

    private val pendingTransactionList = mutableListOf<TransactionWithCustomer>() // Replace Transaction with your model class


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        customerDatabaseHelper = CustomerDatabaseHelper(this)

        // Set up RecyclerView
        binding.pendingRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load all pending transactions initially
        loadPendingTransactions(null, null)

        val totalP = pendingTransactionList.sumOf { it.amount }
        binding.TotalPending.text = "₹$totalP"

        // Set up date picker for start date
        binding.startDateTextView.setOnClickListener {
            showDatePicker { selectedDate ->
                binding.startDateTextView.text = selectedDate
            }
        }

        // Set up date picker for end date
        binding.endDateTextView.setOnClickListener {
            showDatePicker { selectedDate ->
                binding.endDateTextView.text = selectedDate
            }
        }

        // Set Filter Button Listener
        binding.filterButton.setOnClickListener {
            val startDate = binding.startDateTextView.text.toString()
            val endDate = binding.endDateTextView.text.toString()

            if (startDate == "Select Start Date" || endDate == "Select End Date") {
                Toast.makeText(this, "Please select start and end dates!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            // Validate and filter transactions
            loadPendingTransactions(startDate, endDate)
        }

        binding.downloadPdfButton.setOnClickListener {
            if (pendingTransactionList.isNotEmpty()) {
                generatePdf(pendingTransactionList)
            } else {
                Toast.makeText(this, "No transactions to export", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to load pending transactions with optional date filters
    private fun loadPendingTransactions(startDate: String?, endDate: String?) {
        val allTransactionsWithCustomer = customerDatabaseHelper.getAllTransactionsWithCustomer()
        val pendingTransactions = allTransactionsWithCustomer.filter { it.status == "Pending" }

        // Apply date filtering if startDate and endDate are provided
        val filteredTransactions = if (startDate != null && endDate != null) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val start = dateFormat.parse(startDate)
            val end = dateFormat.parse(endDate)

            pendingTransactions.filter { transaction ->
                val transactionDate = dateFormat.parse(transaction.date)
                transactionDate in start..end
            }
        } else {
            pendingTransactions
        }
        pendingTransactionList.clear()
        pendingTransactionList.addAll(filteredTransactions)

        val totalP = filteredTransactions.sumOf { it.amount }
        binding.TotalPending.text = "₹$totalP"

        // Update RecyclerView adapter
        val adapter = TransactionAdapter(filteredTransactions)
        binding.pendingRecyclerView.adapter = adapter

        if (filteredTransactions.isEmpty()) {
            Toast.makeText(this, "No pending transactions found!", Toast.LENGTH_SHORT).show()
        }
    }


    // Show DatePickerDialog
    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Format date to yyyy-MM-dd
            val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            onDateSelected(formattedDate)
        }, year, month, day)

        datePickerDialog.show()
    }


    private fun generatePdf(transactionList: List<TransactionWithCustomer>) {
        val pdfDocument = PdfDocument()
        val pageHeight = 600 + (transactionList.size * 20)
        val pageInfo = PdfDocument.PageInfo.Builder(300, pageHeight, 1).create()
//        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Starting point for writing text
        var yPosition = 50
        val paint = Paint()
        paint.textSize = 12f
        paint.color = Color.BLACK

        // Write each transaction to the PDF
        canvas.drawText("Pending Transactions:", 10f, yPosition.toFloat(), paint)
        yPosition += 20

        transactionList.forEach { transaction ->
            val transactionText = "${transaction.customerName}    -    ₹${transaction.amount}      (${transaction.date})"
            canvas.drawText(transactionText, 10f, yPosition.toFloat(), paint)
            yPosition += 20
        }

        pdfDocument.finishPage(page)

        // Save the PDF
//        val fileName = "PendingTransactions_${System.currentTimeMillis()}.pdf"
//        val filePath = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        val fileName = "PendingTransactions ${System.currentTimeMillis()}.pdf"

        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val filePath = File(downloadsFolder, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(filePath))
            Toast.makeText(this, "PDF saved to ${filePath.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

}
