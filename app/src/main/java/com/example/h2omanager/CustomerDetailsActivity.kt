package com.example.h2omanager

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.h2omanager.databinding.ActivityCustomerDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import android.net.Uri

class CustomerDetailsActivity : AppCompatActivity() {

    private lateinit var db: CustomerDatabaseHelper
    private lateinit var binding: ActivityCustomerDetailsBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        const val REQUEST_CODE_ADD_PAYMENT = 1001
    }

//    private var customerId: Int = -1
    private var count :Int = 0;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCustomerDetailsBinding.inflate(layoutInflater)



        setContentView(binding.root)

        db = CustomerDatabaseHelper(this)

        val updateButton: ImageView = findViewById(R.id.editButton)
        val deleteButton: ImageView = findViewById(R.id.deleteCustomer)


        val customerId = intent.getIntExtra("customer_id", -1)



        if (customerId != -1) {

            val customer = db.getCustomerById(customerId)
            if (customer != null) {
                findViewById<TextView>(R.id.nameTv).text = customer.name
                findViewById<TextView>(R.id.numberTv).text = customer.number
            } else {
                Toast.makeText(this, "Customer not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        val customer = db.getCustomerById(customerId)
        val userId = auth.currentUser?.uid



        updateButton.setOnClickListener {
            val intent = Intent(this, UpdateActivity::class.java).apply {
                if (customer != null) {
                    putExtra("customer_id", customer.id)
                }
            }
            startActivity(intent)
        }
        // Add Payment Button Listener
        binding.adPaymentBtn.setOnClickListener {
            val intent = Intent(this, AddPaymentActivity::class.java).apply {

                if (customer != null) {
                    putExtra("customerId", customer.id)
                    putExtra("customerName", customer.name)
                    putExtra("customerNumber", customer.number)
                }
            }

            startActivity(intent)
        }

        deleteButton.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Contact")
            builder.setMessage("Do you want to delete this contact?")

            builder.setPositiveButton("Yes") { dialog, _ ->

                if (customer != null && userId != null) {
                    db.deleteCustomer(customer.id, userId)
                }
                Toast.makeText(this, "Customer Deleted", Toast.LENGTH_SHORT).show()
                finish()
                dialog.dismiss()
            }

            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // Close the dialog
            }

            // Show the dialog
            val dialog = builder.create()
            dialog.show()

        }

        if (customerId != -1) {

            if (customer != null) {
                binding.bottleCount.text = customer.bottleCount.toString()

            }
        }

        //total bottle Count***************************************************************************************
        val btnMinus: ImageView = findViewById(R.id.btnMinus)
        val btnPlus: ImageView = findViewById(R.id.btnPlus)
        val bottleCount: TextView = findViewById(R.id.bottleCount)

        val xy = db.getCustomerById(customerId)

        if (xy != null) {
            var count = xy.bottleCount

            // Decrement button functionality
            btnMinus.setOnClickListener {
                if (count > 0) {  // Prevent count from going below 0
                    count--
                    bottleCount.text = count.toString()
                    if (customer != null) {
                        val updatedCustomer =
                            Customer(customerId, customer.name, customer.number, count)

                        if (userId != null) {
                            db.updateCustomer(updatedCustomer, userId)
                        }
                    }
                }
            }

            // Increment button functionality
            btnPlus.setOnClickListener {
                count++
                bottleCount.text = count.toString()

                if (customer != null) {
                    val updatedCustomer =
                        Customer(customerId, customer.name, customer.number, count)

                    if (userId != null) {
                        db.updateCustomer(updatedCustomer, userId)
                    }
                }
            }


        }


        //          whatsapp implementation


        binding.btnWhatsApp.setOnClickListener {
            if (customer != null) {
                val pendingTransactions = db.getTransactionsByCustomerId(customerId)
                    .filter { it.status == "Pending" }

                val pendingDetails = pendingTransactions.joinToString("\n") { transaction ->
                    "- ₹${transaction.amount} on ${transaction.date}"
                }

                val message = """
            You have pending amount:
            $pendingDetails
            and ${customer.bottleCount} bottle(s).
            Please clear your dues.
        """.trimIndent()

                val uri = Uri.parse("https://wa.me/${customer.number}?text=${Uri.encode(message)}")
                val intent = Intent(Intent.ACTION_VIEW, uri)

                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "WhatsApp is not installed on this device.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "Customer details are not available.", Toast.LENGTH_SHORT)
                    .show()
            }


        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_ADD_PAYMENT && resultCode == Activity.RESULT_OK) {
            loadCustomerData() // Reload transactions and refresh UI
        }
    }



    override fun onResume() {
        super.onResume()

        loadCustomerData()
    }

    // Function to load or refresh customer data
    private fun loadCustomerData() {
        val customerId = intent.getIntExtra("customer_id", -1)
        setupRecyclerView(customerId)
        if (customerId != -1) {
            val customer = db.getCustomerById(customerId)
            if (customer != null) {
                findViewById<TextView>(R.id.nameTv).text = customer.name
                findViewById<TextView>(R.id.numberTv).text = customer.number

                setupRecyclerView(customerId)
            } else {
                Toast.makeText(this, "Customer not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

//    private fun setupRecyclerView(customerId: Int) {
//        val transactions = db.getTransactionsByCustomerId(customerId) // Fetch transactions from database
//
//        val sortedTransactions = transactions.sortedByDescending { it.date }
//
//        val adapter = CustomerTransactionAdaptor(sortedTransactions)
//        binding.customerTransaction.adapter = adapter
//        binding.customerTransaction.layoutManager = LinearLayoutManager(this)
//
//        val pendingAmount = transactions
//            .filter { it.status == "Pending" }
//            .sumOf { it.amount }
//
//        binding.duesAmount.text = String.format("₹%.2f", pendingAmount)
//    }

    private fun setupRecyclerView(customerId: Int) {
        val transactions = db.getTransactionsByCustomerId(customerId) // Fetch transactions from the database

        val sortedTransactions = transactions.sortedByDescending { it.date }

        val adapter = CustomerTransactionAdaptor(sortedTransactions) { transaction ->
            deleteTransaction(transaction) // Pass the selected transaction for deletion
        }

        binding.customerTransaction.adapter = adapter
        binding.customerTransaction.layoutManager = LinearLayoutManager(this)

        val pendingAmount = transactions
            .filter { it.status == "Pending" }
            .sumOf { it.amount }

        binding.duesAmount.text = String.format("₹%.2f", pendingAmount)
    }




    fun deleteTransaction(transaction: Transaction) {
        val userId = auth.currentUser?.uid ?: return

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Transaction")
        builder.setMessage("Are you sure you want to delete this transaction?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            db.deleteTransaction(transaction.transactionId) // Delete from SQLite
            db.deleteTransactionFromFirebase(transaction.customerId, transaction.transactionId, userId) // Delete from Firebase

            Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
            loadCustomerData() // Refresh the data to update the UI
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }




}