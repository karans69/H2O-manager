package com.example.h2omanager

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.h2omanager.databinding.ActivityExpenseBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class ExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseBinding
    private lateinit var expenseDatabaseHelper: ExpenseDatabaseHelper
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        expenseDatabaseHelper = ExpenseDatabaseHelper(this)
        binding.pendingRecyclerView.layoutManager = LinearLayoutManager(this)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            databaseReference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.uid)
                .child("expenses")
        } else {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadExpenses(null,null)

        binding.AddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        binding.imageView19.alpha = 0.2f

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


        binding.filterButton.setOnClickListener {
            val startDate = binding.startDateTextView.text.toString()
            val endDate = binding.endDateTextView.text.toString()

            if (startDate == "Select Start Date" || endDate == "Select End Date") {
                Toast.makeText(this, "Please select start and end dates!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate and filter transactions
            loadExpenses(startDate, endDate)
        }


    }

    private fun loadExpenses(startDate: String?, endDate: String?) {
        val expenses = expenseDatabaseHelper.getAllExpenses()
        expenseAdapter = ExpenseAdapter(expenses) { expense ->
            showDeleteConfirmationDialog(expense)
        }
        binding.pendingRecyclerView.adapter = expenseAdapter

        // Update total expenses
        val totalExpense = expenses.sumOf { it.amount }
        binding.totalExpenseTextView.text = "₹$totalExpense"
    }

    private fun showDeleteConfirmationDialog(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Yes") { _, _ ->
                deleteExpense(expense)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteExpense(expense: Expense) {
        if (expense.firebaseId != null) {
            Log.d("DeleteExpense", "Deleting expense with Firebase ID: ${expense.firebaseId}")
            databaseReference.child(expense.firebaseId).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Expense deleted from Firebase!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Failed to delete from Firebase: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.d("DeleteExpense", "Firebase ID is null, skipping Firebase deletion.")
        }

        expenseDatabaseHelper.deleteExpense(expense.id)
        Toast.makeText(this, "Expense deleted successfully", Toast.LENGTH_SHORT).show()
        loadExpenses(null, null) // Refresh the list
    }



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

    override fun onResume() {
        super.onResume()
        loadExpenses(null,null) // Refresh data when returning to this activity
    }
}