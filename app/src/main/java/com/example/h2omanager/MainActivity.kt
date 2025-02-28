package com.example.h2omanager

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.h2omanager.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var expenseDatabaseHelper: ExpenseDatabaseHelper
    private lateinit var customerDatabaseHelper: CustomerDatabaseHelper






    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)




        expenseDatabaseHelper = ExpenseDatabaseHelper(this)

        customerDatabaseHelper = CustomerDatabaseHelper(this)

        val currentStock = customerDatabaseHelper.getBottleStock()
        binding.bottleStockText.text= "Bottle Stock: $currentStock"

        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

//        binding.paidText.text = customerDatabaseHelper.getTotalByStatus("Paid").toString()
//        binding.pendingText.text = customerDatabaseHelper.getTotalByStatus("Pending").toString()


        binding.userProfile.setOnClickListener {
            intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }
        binding.AllCustom.setOnClickListener{
            intent = Intent(this, CustomerActivity::class.java)
            startActivity(intent)
        }
        binding.PendingCardView.setOnClickListener{
            intent = Intent(this, PendingActivity::class.java)
            startActivity(intent)
        }
        binding.PaidCard.setOnClickListener{
            intent = Intent(this, PaidActivity::class.java)
            startActivity(intent)
        }
        binding.ExpenseCard.setOnClickListener {
            intent = Intent(this,ExpenseActivity::class.java)
            startActivity(intent)
        }

        binding.cardView.setOnClickListener {
            intent = Intent(this, AllTransactionActivity::class.java)
            startActivity(intent)
        }
        binding.TotalCard.setOnClickListener {
            intent = Intent(this, DailyTransactionSummaryActivity::class.java)
            startActivity(intent)
        }



        binding.cvv.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Edit Bottle Stock")

            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_NUMBER
            builder.setView(input)

            builder.setPositiveButton("Update") { _, _ ->
                val newStock = input.text.toString().toIntOrNull()
                if (newStock != null) {
                    customerDatabaseHelper.updateBottleStock(newStock)
                    binding.bottleStockText.text= "Bottle Stock: $newStock"
                } else {
                    Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

            builder.create().show()
        }

//        val expenses = expenseDatabaseHelper.getAllExpenses()
//        val totalExpense = expenses.sumOf { it.amount }
//        binding.TotalExpenseText.text = totalExpense.toString()


        val videoView: VideoView = findViewById(R.id.videoView)

        // Set the video path
        val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.background_animation)
        videoView.setVideoURI(videoUri)

        // Start the video
        videoView.start()

        // Set looping
        videoView.setOnPreparedListener { it.isLooping = true }



    }


    override fun onResume() {
        super.onResume()
//        refreshTransactionTotals()
//
//        val expenses = expenseDatabaseHelper.getAllExpenses()
//        val totalExpense = expenses.sumOf { it.amount }
//        binding.TotalExpenseText.text = totalExpense.toString()

        val videoView: VideoView = findViewById(R.id.videoView)

        // Set the video path
        val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.background_animation)
        videoView.setVideoURI(videoUri)

        // Start the video
        videoView.start()

        // Set looping
        videoView.setOnPreparedListener { it.isLooping = true }

        val currentStock = customerDatabaseHelper.getBottleStock()
        binding.bottleStockText.text= "Bottle Stock: $currentStock"
    }

    private fun refreshTransactionTotals() {
        val totalPaid = customerDatabaseHelper.getTotalByStatus("paid")
        val totalPending = customerDatabaseHelper.getTotalByStatus("pending")



//        binding.paidText.text = totalPaid.toString()
//        binding.pendingText.text = totalPending.toString()
    }







}

