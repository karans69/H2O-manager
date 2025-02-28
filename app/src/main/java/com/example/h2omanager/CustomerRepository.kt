package com.example.h2omanager

import com.google.firebase.database.FirebaseDatabase


class CustomerRepository(
    private val customerDatabaseHelper: CustomerDatabaseHelper,
    private val expenseDatabaseHelper: ExpenseDatabaseHelper
) {



    fun syncFromFirebase(userId: String, onComplete: (Boolean) -> Unit) {
        val firebaseRef = FirebaseDatabase.getInstance().getReference("users/$userId/customers")

        firebaseRef.get().addOnSuccessListener { snapshot ->
            // Clear local database
            customerDatabaseHelper.clearDatabase()

            // Iterate through customers
            for (customerSnapshot in snapshot.children) {
                val customer = customerSnapshot.getValue(Customer::class.java)

                if (customer != null) {
                    // Insert customer into the local database
                    customerDatabaseHelper.insertContactT(customer, userId)

                    // Sync nested transactions
                    val transactionsSnapshot = customerSnapshot.child("transactions")
                    for (transactionSnapshot in transactionsSnapshot.children) {
                        val transaction = transactionSnapshot.getValue(Transaction::class.java)
                        if (transaction != null) {
                            customerDatabaseHelper.insertTransaction(transaction)
                        }
                    }
                }
            }

            onComplete(true) // Notify sync success
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            onComplete(false) // Notify sync failure
        }
    }

    fun syncExpensesFromFirebase(userId: String, onComplete: (Boolean) -> Unit) {
        val firebaseRef = FirebaseDatabase.getInstance().getReference("users/$userId/expenses")

        firebaseRef.get().addOnSuccessListener { snapshot ->
            // Clear local expenses table
            expenseDatabaseHelper.clearExpenses()

            // Iterate through expenses
            for (expenseSnapshot in snapshot.children) {
                val expense = expenseSnapshot.getValue(Expense::class.java)
                if (expense != null) {
                    // Insert expense into local database
                    expenseDatabaseHelper.addExpense(expense)
                }
            }

            onComplete(true) // Notify sync success
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            onComplete(false) // Notify sync failure
        }
    }

}




    // Sync data from Firebase to SQLite
//    fun syncFromFirebase(userId: String, onComplete: (Boolean) -> Unit) {
//        val firebaseRef = FirebaseDatabase.getInstance().getReference("users/$userId/customers")
//
//        firebaseRef.get().addOnSuccessListener { snapshot ->
//            // Clear local database
//            customerDatabaseHelper.clearDatabase()
//
//            // Save data from Firebase to SQLite
//            for (child in snapshot.children) {
//                val customer = child.getValue(Customer::class.java)
//                if (customer != null) {
//                    customerDatabaseHelper.insertContactT(customer,userId)
//                }
//            }
//            val transactionsSnapshot = snapshot.child("transactions")
//            for (child in transactionsSnapshot.children) {
//                val transaction = child.getValue(Transaction::class.java)
//                if (transaction != null) {
//                    // Parse IDs as integers
//                    val transactionWithIntegers = transaction.copy(
//                        transactionId = child.child("id").value.toString().toInt(),
//                        customerId = child.child("customerId").value.toString().toInt()
//                    )
//                    customerDatabaseHelper.insertTransaction(transactionWithIntegers)
//                }
//            }
//
//
//            onComplete(true) // Notify sync success
//        }.addOnFailureListener {
//            it.printStackTrace()
//            onComplete(false) // Notify sync failure
//        }
//    }
//
//     //Clear all local data
//    fun clearLocalData() {
//        customerDatabaseHelper.clearDatabase()
//    }
//
//    // Retrieve all customers locally
//    fun getAllCustomers(): List<Customer> {
//        return customerDatabaseHelper.getAllCustomer()
//    }
