package com.example.krishisetuapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.krishisetuapp.R

class FarmerAdapter(
    private val list: List<Farmer>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<FarmerAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.txtFarmerId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_farmer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val farmer = list[position]
        holder.txtName.text = farmer.name
        holder.itemView.setOnClickListener {
            onClick(farmer.userId)
        }
    }

    override fun getItemCount() = list.size
}