package com.example.h2omanager

data class Transaction(
    val transactionId: Int =0,
    val customerId: Int = 0,
    val amount: Double = 0.0,
    val date: String ="",
    val status: String = "",

){
    // Default no-argument constructor required by Firebase
    constructor() : this(0, 0, 0.0, "", "")
}
