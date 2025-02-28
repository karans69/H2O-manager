package com.example.h2omanager

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AllTransactionAdapter (
    private val transactionList: List<TransactionWithCustomer>
) : RecyclerView.Adapter<AllTransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerName: TextView = itemView.findViewById(R.id.nameTextView)
        val customerNumber: TextView = itemView.findViewById(R.id.numberTextView)
        val transactionAmount: TextView = itemView.findViewById(R.id.amountTextView)
        val transactionDate: TextView = itemView.findViewById(R.id.dateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]

        holder.customerName.text = transaction.customerName
        holder.customerNumber.text = transaction.customerNumber
        holder.transactionAmount.text = String.format("₹%.2f", transaction.amount)
        holder.transactionDate.text = transaction.date


        if (transaction.status == "Paid") {
            holder.transactionAmount.setTextColor(Color.BLACK) // Black for paid
        } else if (transaction.status == "Pending") {
            holder.transactionAmount.setTextColor(Color.RED) // Red for pending
        }
    }

    override fun getItemCount(): Int = transactionList.size
}