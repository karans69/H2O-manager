package com.example.h2omanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.google.firebase.database.FirebaseDatabase

class CustomerDatabaseHelper  (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME,null,
    DATABASE_VERSION){

    companion object{
        private const val DATABASE_NAME = "aquwasuplay.db"
        private const val DATABASE_VERSION = 4
        private const val TABLE_NAME = "allcustomer"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_BOTTLE_COUNT = "bottle_count"
        private const val COLUMN_NUMBER = "number"
        private const val TABLE2_NAME = "transactions"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_NAME TEXT, $COLUMN_NUMBER TEXT,$COLUMN_BOTTLE_COUNT INTEGER DEFAULT 0)"
        db?.execSQL(createTableQuery)

        val createTransactionTableQuery = """
        CREATE TABLE transactions (
             transactionId INTEGER PRIMARY KEY AUTOINCREMENT,
            customerId INTEGER,
            amount REAL,
            date TEXT,
            status TEXT,
            FOREIGN KEY(customerId) REFERENCES customers(id)
        )
    """
        db?.execSQL(createTransactionTableQuery)

        val createSettingsTable = """
        CREATE TABLE IF NOT EXISTS settings (
            bottleStock INTEGER
        )
    """
        db?.execSQL(createSettingsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
//        db?.execSQL(dropTableQuery)
//        db?.execSQL("DROP TABLE IF EXISTS transactions")
//        onCreate(db)
        if (oldVersion < 2) {
            // Add the transactions table
            val createTransactionTableQuery = """
            CREATE TABLE transactions (
                 transactionId INTEGER PRIMARY KEY AUTOINCREMENT,
                customerId INTEGER, 
                amount REAL, 
                date TEXT, 
                status TEXT, 
                FOREIGN KEY(customerId) REFERENCES customers(id)
            )
        """
            db?.execSQL(createTransactionTableQuery)

            val createSettingsTable = """
        CREATE TABLE IF NOT EXISTS settings (
            bottleStock INTEGER
        )
    """
            db?.execSQL(createSettingsTable)
        }
    }

    fun insertContact(customer: Customer, userId: String) {
        val db = writableDatabase

        // Insert into local database
        val values = ContentValues().apply {
            put(COLUMN_NAME, customer.name)
            put(COLUMN_NUMBER, customer.number)
            put(COLUMN_BOTTLE_COUNT, customer.bottleCount)
        }
        val newId = db.insert(TABLE_NAME, null, values) // SQLite generates an ID
        db.close()

        if (newId != -1L) {
            // Use the SQLite ID as the Firebase key
            val firebaseRef = FirebaseDatabase.getInstance().getReference("users/$userId/customers")
            val firebaseCustomer = customer.copy(id = newId.toInt()) // Use the SQLite ID
            firebaseRef.child(newId.toString()).setValue(firebaseCustomer)
                .addOnSuccessListener {
                    Log.d("Firebase", "Customer added successfully to Firebase.")
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to add customer to Firebase: ${e.message}")
                }
        } else {
            Log.e("SQLite", "Failed to insert customer into local database.")
        }
    }


    fun insertContactT(customer: Customer, userId: String) {
        val db = writableDatabase

        // Insert into local database
        val values = ContentValues().apply {
            put(COLUMN_NAME, customer.name)
            put(COLUMN_NUMBER, customer.number)
            put(COLUMN_BOTTLE_COUNT,customer.bottleCount)
        }
        db.insert(TABLE_NAME, null, values) // SQLite auto-generates an ID
        db.close()

    }

//    fun insertTransaction(transaction: Transaction) {
//        val db = writableDatabase
//
//        // Insert transaction into local database
//        val values = ContentValues().apply {
//            put("transactionId", transaction.transactionId)
//            put("customerId", transaction.customerId)
//            put("amount", transaction.amount)
//            put("date", transaction.date)
//            put("status", transaction.status)
//        }
//        db.insert("transactions", null, values)
//        db.close()
//    }

    fun insertTransaction(transaction: Transaction) {
        val db = writableDatabase

        // Check if the transaction already exists
        val query = "SELECT COUNT(*) FROM transactions WHERE transactionId = ?"
        val cursor = db.rawQuery(query, arrayOf(transaction.transactionId.toString()))
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        if (count > 0) {
            // Update existing transaction
            val values = ContentValues().apply {
                put("customerId", transaction.customerId)
                put("amount", transaction.amount)
                put("date", transaction.date)
                put("status", transaction.status)
            }
            db.update("transactions", values, "transactionId = ?", arrayOf(transaction.transactionId.toString()))
        } else {
            // Insert new transaction
            val values = ContentValues().apply {
                put("transactionId", transaction.transactionId)
                put("customerId", transaction.customerId)
                put("amount", transaction.amount)
                put("date", transaction.date)
                put("status", transaction.status)
            }
            db.insert("transactions", null, values)
        }

        db.close()
    }




    fun isNumberExists(number: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM allcustomer WHERE number = ?"
        val cursor = db.rawQuery(query, arrayOf(number))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getAllCustomer(): List<Customer> {
        val customerList = mutableListOf<Customer>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query,null)

        while (cursor.moveToNext()){
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val number = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NUMBER))
            val bottle = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOTTLE_COUNT))

            val customer = Customer(id,name,number,bottle)
            customerList.add(customer)
        }
        cursor.close()
        db.close()

        return customerList
    }

    fun getCustomerById(id: Int): Customer? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(id.toString()))

        var customer: Customer? = null
        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val number = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NUMBER))
            val bottleCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOTTLE_COUNT))
            customer = Customer(id, name, number,bottleCount)
        }
        cursor.close()
        db.close()

        return customer
    }



    fun updateCustomer(customer: Customer, userId: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, customer.name)
            put(COLUMN_NUMBER, customer.number)
            put(COLUMN_BOTTLE_COUNT, customer.bottleCount)
        }
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(customer.id.toString())
        db.update(TABLE_NAME, values, whereClause, whereArgs)
        db.close()

        // Update in Firebase using the SQLite ID as the key
        val firebaseRef = FirebaseDatabase.getInstance().getReference("users/$userId/customers")
        firebaseRef.child(customer.id.toString()).setValue(customer)
            .addOnSuccessListener {
                Log.d("Firebase", "Customer updated successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to update customer: ${e.message}")
            }
    }


    fun deleteCustomer(customerId: Int, userId: String){
        val db = writableDatabase
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(customerId.toString())
        db.delete(TABLE_NAME,whereClause,whereArgs)
        db.delete(TABLE2_NAME,"customerId = ?", arrayOf(customerId.toString()))
        db.close()

        val firebaseRef = FirebaseDatabase.getInstance().getReference("users/$userId/customers")
        firebaseRef.child(customerId.toString()).removeValue()
    }

    fun clearDatabase() {
        val db = writableDatabase
        db.execSQL("DELETE FROM allcustomer")
        db.execSQL("DELETE FROM transactions")
        db.close()
    }



    fun addTransaction(transaction: Transaction, userId: String) {
        val db = writableDatabase

        // Save the transaction in SQLite
        val values = ContentValues().apply {
            put("customerId", transaction.customerId)
            put("amount", transaction.amount)
            put("date", transaction.date)
            put("status", transaction.status)
        }

        val sqliteTransactionId = db.insert("transactions", null, values).toInt() // Get the SQLite-generated transactionId

        // Add the transaction to Firebase
        val firebaseRef = FirebaseDatabase.getInstance()
            .getReference("users/$userId/customers/${transaction.customerId}/transactions")

        val newTransaction = transaction.copy(transactionId = sqliteTransactionId)

        firebaseRef.child(sqliteTransactionId.toString()).setValue(newTransaction)
            .addOnSuccessListener {
                // Optional: Handle success if needed
                Log.d("AddTransaction", "Transaction added successfully to Firebase.")
            }
            .addOnFailureListener { exception ->
                // Optional: Handle error if needed
                Log.e("AddTransaction", "Failed to add transaction to Firebase: ${exception.message}")
            }

        db.close()
    }





    fun getTransactionsByCustomerId(customerId: Int): List<Transaction> {
        val transactionList = mutableListOf<Transaction>()
        val db = readableDatabase
        val query = "SELECT * FROM transactions WHERE customerId = ?"
        val cursor = db.rawQuery(query, arrayOf(customerId.toString()))

        while (cursor.moveToNext()) {
            val transactionId = cursor.getInt(cursor.getColumnIndexOrThrow("transactionId"))
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))
            val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
            val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))

            val transaction = Transaction(transactionId, customerId, amount, date, status)
            transactionList.add(transaction)
        }
        cursor.close()
        db.close()

        return transactionList
    }

//    fun deleteTransaction(transactionId: Int) {
//        val db = writableDatabase
//        db.delete("transactions", "transactionId = ?", arrayOf(transactionId.toString()))
//        db.close()
//        val firebaseRef = FirebaseDatabase.getInstance()
//        val firebaseRef = FirebaseDatabase.getInstance().getReference("users/$userId/customers/$customerId/transactions/$transactionId")
//
//    }



    fun getAllTransactionsWithCustomer(): List<TransactionWithCustomer> {
        val transactionsWithCustomer = mutableListOf<TransactionWithCustomer>()
        val db = readableDatabase

        val transactionQuery = "SELECT * FROM transactions"
        val cursor = db.rawQuery(transactionQuery, null)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val transactionId = cursor.getInt(cursor.getColumnIndexOrThrow("transactionId"))
                val customerId = cursor.getInt(cursor.getColumnIndexOrThrow("customerId"))
                val amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))

                // Fetch customer details for the transaction
                val customer = getCustomerById(customerId)
                if (customer != null) {
                    val transactionWithCustomer = TransactionWithCustomer(
                        transactionId = transactionId,
                        customerId = customerId,
                        customerName = customer.name,
                        customerNumber = customer.number,
                        amount = amount,
                        date = date,
                        status = status
                    )
                    transactionsWithCustomer.add(transactionWithCustomer)
                }
            } while (cursor.moveToNext())
        }
        cursor?.close()
        db.close()

        return transactionsWithCustomer
    }



    fun deleteTransaction(transactionId: Int) {
        val db = writableDatabase
        db.delete("transactions", "transactionId = ?", arrayOf(transactionId.toString()))
        db.close()
    }

    fun deleteTransactionFromFirebase(customerId: Int, transactionId: Int, userId: String) {
        val firebaseRef = FirebaseDatabase.getInstance()
            .getReference("users/$userId/customers/$customerId/transactions/$transactionId")

        firebaseRef.removeValue()
            .addOnSuccessListener {
                Log.d("DeleteTransaction", "Transaction deleted successfully from Firebase.")
            }
            .addOnFailureListener { exception ->
                Log.e("DeleteTransaction", "Failed to delete transaction from Firebase: ${exception.message}")
            }
    }




    //totals
    fun getTotalByStatus(status: String): Double {
        val db = readableDatabase
        var total = 101.0

        val query = "SELECT SUM(amount) AS total FROM transactions WHERE status = ?"
        val cursor = db.rawQuery(query, arrayOf(status))

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"))
        }
        cursor.close()
        db.close()

        return total
    }


    fun getTransactionsByDate(date: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT * FROM transactions WHERE date = ?", arrayOf(date))

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val transactionId = cursor.getInt(cursor.getColumnIndexOrThrow("transactionId"))
                val customerId = cursor.getInt(cursor.getColumnIndexOrThrow("customerId"))
                val amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))
                val transactionDate = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))

                transactions.add(Transaction(transactionId, customerId, amount, transactionDate, status))
            } while (cursor.moveToNext())
        }

        cursor?.close()
        db.close()

        return transactions
    }

    fun getTransactionsWithCustomerByDate(date: String): List<TransactionWithCustomer> {
        return getAllTransactionsWithCustomer().filter { it.date == date }
    }


    //////////////////////////////////////////////////stock db

//    fun updateBottleStock(stock: Int) {
//        val db = writableDatabase
//        val values = ContentValues().apply {
//            put("bottleStock", stock)
//        }
//        db.update("settings", values, null, null) // Assume a single-row settings table
//        db.close()
//    }

    fun updateBottleStock(stock: Int) {
        val db = writableDatabase

        // Check if a row exists
        val cursor = db.rawQuery("SELECT COUNT(*) FROM settings", null)
        var exists = false
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0
        }
        cursor.close()

        if (exists) {
            // Update the existing row
            val values = ContentValues().apply {
                put("bottleStock", stock)
            }
            db.update("settings", values, null, null)
        } else {
            // Insert a new row if none exists
            val values = ContentValues().apply {
                put("bottleStock", stock)
            }
            db.insert("settings", null, values)
        }
        db.close()
    }


//    fun getBottleStock(): Int {
//        val db = readableDatabase
//        val cursor = db.rawQuery("SELECT bottleStock FROM settings", null)
//        var stock = 0
//        if (cursor.moveToFirst()) {
//            stock = cursor.getInt(cursor.getColumnIndexOrThrow("bottleStock"))
//        }
//        cursor.close()
//        db.close()
//        return stock
//    }

    fun getBottleStock(): Int {
        val db = this.readableDatabase
        return try {
            val cursor = db.rawQuery("SELECT bottleStock FROM settings LIMIT 1", null)
            if (cursor.moveToFirst()) {
                cursor.getInt(0)
            } else {
                0 // Default stock if no entry exists
            }.also { cursor.close() }
        } catch (e: SQLiteException) {
            // Log error and return default
            Log.e("DatabaseError", "Error querying settings table", e)
            0
        }
    }



    fun getDailyTransactionSummary(): List<DailyTransactionSummary> {
        val summaries = mutableListOf<DailyTransactionSummary>()
        val query = """
        SELECT 
            date, 
            SUM(CASE WHEN status = 'Pending' THEN amount ELSE 0 END) AS pendingTotal,
            SUM(CASE WHEN status = 'Paid' THEN amount ELSE 0 END) AS paidTotal
        FROM transactions
        GROUP BY date
        ORDER BY date DESC
    """.trimIndent()

        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                val pendingTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("pendingTotal"))
                val paidTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("paidTotal"))
                summaries.add(DailyTransactionSummary(date, pendingTotal, paidTotal))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return summaries
    }







}