package com.example.h2omanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ExpenseDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "expenses.db"
        private const val DATABASE_VERSION = 3
        private const val TABLE_NAME = "expenses"
        private const val COLUMN_ID = "id"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_REASON = "reason"
        private const val COLUMN_FIREBASE_ID = "firebaseId"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_REASON TEXT,
                $COLUMN_FIREBASE_ID TEXT
            )
        """.trimIndent()
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)

    }

    fun addExpense(expense: Expense): Long {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_AMOUNT, expense.amount)
                put(COLUMN_DATE, expense.date)
                put(COLUMN_REASON, expense.reason)
                put(COLUMN_FIREBASE_ID, expense.firebaseId) // Save Firebase ID
            }
            db.insert("expenses", null, values)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            -1L // Return -1 to indicate failure
        } finally {
            db.close() // Ensure database resources are released
        }
    }



    fun clearExpenses() {
        val db = writableDatabase
        db.execSQL("DELETE FROM Expenses")
        db.close()
    }

    fun getAllExpenses(): List<Expense> {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "$COLUMN_DATE DESC")
        val expenses = mutableListOf<Expense>()

        with(cursor) {
            while (moveToNext()) {
                expenses.add(
                    Expense(
                        id = getInt(getColumnIndexOrThrow(COLUMN_ID)),
                        amount = getDouble(getColumnIndexOrThrow(COLUMN_AMOUNT)),
                        date = getString(getColumnIndexOrThrow(COLUMN_DATE)),
                        reason = getString(getColumnIndexOrThrow(COLUMN_REASON)),
                        firebaseId = getString(getColumnIndexOrThrow(COLUMN_FIREBASE_ID)) // Retrieve firebaseId
                    )
                )
            }
            close()
        }
        return expenses
    }



    fun deleteExpense(expenseId: Int) {
        val db = writableDatabase
        db.delete("expenses", "id=?", arrayOf(expenseId.toString()))
        db.close()
    }

    fun syncExpensesFromFirebase(userId: String, onComplete: (Boolean) -> Unit) {
        val firebaseRef = FirebaseDatabase.getInstance().getReference("users/$userId/expenses")
        firebaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear local expenses table
                clearExpenses()

                for (expenseSnapshot in snapshot.children) {
                    val expense = expenseSnapshot.getValue(Expense::class.java)
                    if (expense != null) {
                        addExpense(expense)
                    }
                }
                onComplete(true) // Notify sync success
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseSync", "Error syncing expenses: ${error.message}")
                onComplete(false) // Notify sync failure
            }
        })
    }


    fun updateFirebaseId(expenseId: Int, firebaseId: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("firebaseId", firebaseId)
        }
        db.update("expenses", values, "id=?", arrayOf(expenseId.toString()))
        db.close()
    }





}