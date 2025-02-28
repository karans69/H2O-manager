package com.example.h2omanager

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomerTransactionAdaptor(
    private val transactionList: List<Transaction>,
    private val onDeleteTransaction: (Transaction) -> Unit
) : RecyclerView.Adapter<CustomerTransactionAdaptor.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionDate: TextView = itemView.findViewById(R.id.transactionDate)
        val transactionAmount: TextView = itemView.findViewById(R.id.transactionAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]

        holder.transactionDate.text = transaction.date
        holder.transactionAmount.text = transaction.amount.toString()

        // Set the amount color based on the status
        if (transaction.status == "Paid") {
            holder.transactionAmount.setTextColor(Color.BLACK) // Black for paid
        } else if (transaction.status == "Pending") {
            holder.transactionAmount.setTextColor(Color.RED) // Red for pending
        }else if (transaction.status == "Advance") {
            holder.transactionAmount.setTextColor(Color.GREEN) // Red for pending
        }

        holder.itemView.setOnLongClickListener {
            onDeleteTransaction(transaction)
            true
        }


    }

    override fun getItemCount(): Int = transactionList.size
}