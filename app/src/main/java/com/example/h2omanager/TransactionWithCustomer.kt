package com.example.h2omanager

data class TransactionWithCustomer(
    val transactionId: Int,
    val customerId: Int,
    val customerName: String,
    val customerNumber: String,
    val amount: Double,
    val date: String,
    val status: String
)
