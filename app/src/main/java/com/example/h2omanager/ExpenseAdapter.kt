package com.example.h2omanager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.h2omanager.databinding.ExpenseItemBinding

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private val onDeleteExpense: (Expense) -> Unit

) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(private val binding: ExpenseItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            binding.amountTextView.text = "₹${expense.amount}"
            binding.dateTextView.text = expense.date
            binding.reasonTextView.text = expense.reason ?: "No reason provided"

            itemView.setOnLongClickListener {
                onDeleteExpense(expense) // Trigger the delete callback
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ExpenseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount(): Int = expenses.size

    fun updateExpenses(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}