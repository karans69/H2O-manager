package com.example.h2omanager

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomerAdaptor (private var customers:List<Customer>, context: Context) : RecyclerView.Adapter<CustomerAdaptor.CustomerViewHolder>() {


    class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextview)
        val numberTextView: TextView = itemView.findViewById(R.id.numberTextview)
        val btc: TextView = itemView.findViewById(R.id.btc)
        val detailsActivity: LinearLayout = itemView.findViewById(R.id.customerItem)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.customer_item,parent,false)
        return  CustomerViewHolder(view)
    }

    override fun getItemCount(): Int = customers.size

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = customers[position]
        holder.numberTextView.text = customer.number
        holder.nameTextView.text = customer.name
        holder.btc.text = customer.bottleCount.toString()

        holder.detailsActivity.setOnClickListener{
            val intent = Intent(holder.itemView.context, CustomerDetailsActivity::class.java).apply {
                putExtra("customer_id",customer.id)
            }
            holder.itemView.context.startActivity(intent)
        }


    }


    fun refreshData(newCustomer: List<Customer>){
        customers = newCustomer.sortedBy { it.name }
        notifyDataSetChanged()
    }






}