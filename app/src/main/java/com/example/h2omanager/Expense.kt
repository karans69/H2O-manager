package com.example.h2omanager

data class Expense(
    val id: Int = 0,
    val amount: Double,
    val date: String,
    val reason: String? = null,
    val firebaseId: String? = null
){
    // No-argument constructor for Firebase
    constructor() : this(0, 0.0, "", null, null)
}
