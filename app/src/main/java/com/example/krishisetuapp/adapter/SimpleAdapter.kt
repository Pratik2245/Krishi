package com.example.krishisetuapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.krishisetuapp.R
import com.example.krishisetuapp.SoilRecord

class SimpleAdapter(
    private val list: MutableList<SoilRecord>
) : RecyclerView.Adapter<SimpleAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNpk: TextView = view.findViewById(R.id.txtNpk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val r = list[position]

        holder.txtNpk.text = """
📊 BASIC INFO
Moisture: ${r.moisture ?: "--"} %
Temperature: ${r.temperature ?: "--"} °C
pH: ${r.ph ?: "--"}

🌍 SOIL DETAILS
Color: ${r.soilColor ?: "--"}
Type: ${r.soilType ?: "--"}

🧪 NPK PREDICTION
N: ${r.predictedN ?: "--"}
P: ${r.predictedP ?: "--"}
K: ${r.predictedK ?: "--"}

🌱 CROPS
${r.crops?.joinToString(", ") ?: "--"}
        """.trimIndent()
    }

    override fun getItemCount(): Int = list.size
}