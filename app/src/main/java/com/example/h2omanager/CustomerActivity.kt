package com.example.h2omanager

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.h2omanager.databinding.ActivityCustomerBinding

class CustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerBinding
    private lateinit var db:CustomerDatabaseHelper
    private lateinit var customerAdaptor: CustomerAdaptor
    private lateinit var fullCustomerList: List<Customer>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = CustomerDatabaseHelper(this)
        fullCustomerList = db.getAllCustomer()
        customerAdaptor = CustomerAdaptor(db.getAllCustomer(),this)

        binding.customerRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.customerRecyclerView.adapter = customerAdaptor

        binding.addCustomer.setOnClickListener{
            val intent = Intent(this,AddCustomerActivity::class.java)
            startActivity(intent)
        }

        setupSearchBar()

        binding.Bbg2.alpha = 0.1f

    }

    override fun onResume() {
        super.onResume()
        customerAdaptor.refreshData(db.getAllCustomer())
    }

    private fun setupSearchBar() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                if (searchText.isNotEmpty()) {
                    binding.searchProgressBar.visibility = View.VISIBLE
                    filterCustomers(searchText)
                } else {
                    customerAdaptor.refreshData(fullCustomerList)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                binding.searchProgressBar.visibility = View.GONE
            }
        })
    }

    private fun filterCustomers(searchText: String) {
        val filteredList = fullCustomerList.filter {
            it.name.contains(searchText, ignoreCase = true) ||
                    it.number.contains(searchText, ignoreCase = true)
        }
        customerAdaptor.refreshData(filteredList)
    }


}