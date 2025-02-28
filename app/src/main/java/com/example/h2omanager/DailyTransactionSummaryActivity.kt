package com.example.h2omanager

import android.app.DatePickerDialog
import android.app.Dialog
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.h2omanager.databinding.ActivityDailyTransactionSummaryBinding

class DailyTransactionSummaryActivity : AppCompatActivity() {
    private lateinit var adapter: DailyTransactionAdapter
    private lateinit var summaries: List<DailyTransactionSummary>
    private lateinit var databaseHelper: CustomerDatabaseHelper
    private lateinit var binding: ActivityDailyTransactionSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyTransactionSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val recyclerView: RecyclerView = binding.dailyTransactionRecyclerView

        databaseHelper = CustomerDatabaseHelper(this)
        summaries = databaseHelper.getDailyTransactionSummary()

        adapter = DailyTransactionAdapter(summaries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        binding.filterButton.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val datePicker = DatePickerFragment { selectedDate ->
            filterTransactionsByDate(selectedDate)
        }
        datePicker.show(supportFragmentManager, "datePicker")
    }

    private fun filterTransactionsByDate(date: String) {
        val filteredSummaries = summaries.filter { it.date == date }
        adapter = DailyTransactionAdapter(filteredSummaries)
        findViewById<RecyclerView>(R.id.dailyTransactionRecyclerView).adapter = adapter
    }

    class DatePickerFragment(private val onDateSelected: (String) -> Unit) : DialogFragment(), DatePickerDialog.OnDateSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            return DatePickerDialog(requireContext(), this, year, month, day)
        }

        override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(selectedDate)
        }
    }

}