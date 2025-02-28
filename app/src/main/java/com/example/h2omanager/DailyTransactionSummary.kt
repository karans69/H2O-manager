package com.example.h2omanager

data class DailyTransactionSummary(
    val date: String,
    val pendingTotal: Double,
    val paidTotal: Double
)
