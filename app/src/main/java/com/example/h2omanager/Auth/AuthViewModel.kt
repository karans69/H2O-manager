package com.example.h2omanager


import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AuthViewModel  : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()



    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")



    fun signUp(name: String, username: String, number: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(username, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val userMap = mapOf(
                            "name" to name,
                            "email" to username,
                            "number" to number
                        )

                        // Save user details in the database
                        database.child(userId).setValue(userMap)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    onComplete(true, null)
                                } else {
                                    onComplete(false, dbTask.exception?.message ?: "Failed to save user details.")
                                }
                            }
                    } else {
                        onComplete(false, "Failed to retrieve user ID.")
                    }
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun login(username: String, password: String, customerRepository: CustomerRepository, onComplete: (Boolean, String?, Map<String, String?>?) -> Unit) {
        auth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid

                    if (userId != null) {

                        // Fetch user details from Realtime Database
                        database.child(userId).get()
                            .addOnSuccessListener { dataSnapshot ->
                                if (dataSnapshot.exists()) {


                                    // Convert the dataSnapshot into a Map
                                    val userDetails = mapOf(
                                        "name" to dataSnapshot.child("name").value as? String,
                                        "email" to dataSnapshot.child("email").value as? String,
                                        "number" to dataSnapshot.child("number").value as? String
                                    )
                                    customerRepository.syncFromFirebase(userId) { success ->
                                        if (success) {
                                            // Successfully synced customer data
                                            onComplete(true, null, userDetails)
                                        } else {
                                            // Sync failed
                                            onComplete(false, "Failed to sync customer data.", userDetails)
                                        }
                                    }
                                } else {
                                    onComplete(false, "User details not found in database.", null)
                                }
                            }
                            .addOnFailureListener { exception ->
                                onComplete(false, "Failed to fetch user details: ${exception.message}", null)
                            }
                    } else {
                        onComplete(false, "Failed to retrieve user ID.", null)
                    }
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidUserException -> "No account found with this email."
                        is FirebaseAuthInvalidCredentialsException -> "Invalid password."
                        else -> task.exception?.message ?: "Login failed. Try again."
                    }
                    onComplete(false, errorMessage, null)
                }
            }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }



    fun logout(context: Context) {
        auth.signOut()
        context.getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
}
