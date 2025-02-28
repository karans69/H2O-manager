package com.example.h2omanager

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.h2omanager.databinding.ActivityAddExpenseBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class AddExpenseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var expenseDatabaseHelper: ExpenseDatabaseHelper
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        expenseDatabaseHelper = ExpenseDatabaseHelper(this)

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

        binding.editTextDate.setOnClickListener {
            showDatePicker { selectedDate ->
                binding.editTextDate.setText(selectedDate)
            }
        }

        binding.AddIt.setOnClickListener {
            val amountText = binding.paymentInput.text.toString()
            val date = binding.editTextDate.text.toString()
            val reason = binding.editText.text.toString()

            if (amountText.isBlank() || date.isBlank()) {
                Toast.makeText(this, "Amount and Date are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Invalid amount!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expense = Expense(amount = amount, date = date, reason = reason)
            val firebaseId = saveExpenseToFirebase(expense)
            val expenseWithFirebaseId = expense.copy(firebaseId = firebaseId)

            expenseDatabaseHelper.addExpense(expenseWithFirebaseId)
            Toast.makeText(this, "Expense Added!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            onDateSelected(formattedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun saveExpenseToFirebase(expense: Expense): String? {
        val expenseId = databaseReference.push().key // Generate a unique key
        if (expenseId != null) {
            val expenseMap = mapOf(
                "amount" to expense.amount,
                "date" to expense.date,
                "reason" to expense.reason
            )
            databaseReference.child(expenseId).setValue(expenseMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Expense saved to Firebase!", Toast.LENGTH_SHORT).show()
                    expenseDatabaseHelper.updateFirebaseId(expense.id, expenseId)
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Failed to save to Firebase: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
        return expenseId
    }


}