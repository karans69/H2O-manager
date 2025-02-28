package com.example.h2omanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DailyTransactionAdapter (private val summaries: List<DailyTransactionSummary>) :
    RecyclerView.Adapter<DailyTransactionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val pendingTotalTextView: TextView = itemView.findViewById(R.id.pendingTotalTextView)
        val paidTotalTextView: TextView = itemView.findViewById(R.id.paidTotalTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.daily_transaction_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val summary = summaries[position]
        holder.dateTextView.text = summary.date
        holder.pendingTotalTextView.text = String.format("₹%.0f",summary.pendingTotal)
        holder.paidTotalTextView.text = String.format("₹%.0f",summary.paidTotal)
    }

    override fun getItemCount(): Int = summaries.size
}