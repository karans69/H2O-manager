package com.example.h2omanager



import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.h2omanager.AuthViewModel
import com.example.h2omanager.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

//import kotlinx.coroutines.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private val viewModel = AuthViewModel()
    private lateinit var binding: ActivityLoginBinding
    private lateinit var customerDatabaseHelper: CustomerDatabaseHelper
    private lateinit var expenseDatabaseHelper: ExpenseDatabaseHelper
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseAuth.getInstance().setLanguageCode("en")

        customerDatabaseHelper = CustomerDatabaseHelper(this)
        expenseDatabaseHelper = ExpenseDatabaseHelper(this)
        val customerRepository = CustomerRepository(customerDatabaseHelper,expenseDatabaseHelper)  // Create an instance of CustomerRepository

        binding.DontHaveAc.setOnClickListener {
            startActivity(Intent(this, AdminConntactActivity::class.java))
        }

        binding.Loginbtn.setOnClickListener {
            val username = binding.userNameEDT.text.toString()
            val password = binding.passwordEdt.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(username, password, customerRepository) { isSuccess, errorMessage, userDetails ->
                if (isSuccess) {
                    val name = userDetails?.get("name").toString()
                    val email = userDetails?.get("email").toString()
                    val number = userDetails?.get("number").toString()
                    saveUserDetails(name, email, number)

                    val userId = FirebaseAuth.getInstance().currentUser?.uid

                    if (userId != null) {
                        // Sync expenses from Firebase
                        expenseDatabaseHelper.syncExpensesFromFirebase(userId) { expensesSyncSuccess ->
                            if (expensesSyncSuccess) {
                                // Data synced successfully, proceed to main activity
                                //startActivity(Intent(this, MainActivity::class.java))
                                //finish()
                                Toast.makeText(this, "wait", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Failed to sync expense data.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }


                    if (userId != null) {
                        // Sync customer data from Firebase
                        customerRepository.syncFromFirebase(userId) { customerSyncSuccess ->
                            if (customerSyncSuccess) {
                                // Sync expenses data from Firebase
                                customerRepository.syncExpensesFromFirebase(userId) { expensesSyncSuccess ->
                                    if (expensesSyncSuccess) {
                                        // Data synced successfully, proceed to main activity
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Failed to sync expense data.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(this, "Failed to sync customer data.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    Log.d("LoginSuccess", "Name: $name, Email: $email, Number: $number")
                } else {
                    Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }


//    private fun syncCustomerData(userId: String) {
//        customerDatabaseHelper.syncFromFirebase(userId)
//        Toast.makeText(this, "Data synchronized", Toast.LENGTH_SHORT).show()
//    }



    private fun saveUserDetails(name: String, username: String, number: String) {
        val prefs = getSharedPreferences("USER_PREF", MODE_PRIVATE)
        prefs.edit()
            .putString("name", name)
            .putString("username", username)
            .putString("number", number)
            .apply()
    }
}
