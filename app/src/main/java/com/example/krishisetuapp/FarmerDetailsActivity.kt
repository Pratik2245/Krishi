package com.example.krishisetuapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.krishisetuapp.adapter.SimpleAdapter
import com.google.firebase.firestore.FirebaseFirestore

class FarmerDetailsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private val records = mutableListOf<SoilRecord>()
    private lateinit var adapter: SimpleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farmer_details2)

        recyclerView = findViewById(R.id.recyclerRecords)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = SimpleAdapter(records)
        recyclerView.adapter = adapter

        val userId = intent.getStringExtra("USER_ID")
        userId?.let { loadRecords(it) }
    }

    private fun loadRecords(userId: String) {
        db.collection("soil_records")
            .whereEqualTo("user_id", userId)
            .get()
            .addOnSuccessListener { result ->

                records.clear()

                for (doc in result) {
                    val record = SoilRecord(
                        moisture = doc.getDouble("moisture"),
                        ph = doc.getDouble("ph"),
                        temperature = doc.getDouble("temperature"),
                        soilColor = doc.getString("soil_color"),
                        soilType = doc.getString("soil_type"),
                        predictedN = doc.getDouble("predicted_N"),
                        predictedP = doc.getDouble("predicted_P"),
                        predictedK = doc.getDouble("predicted_K"),
                        crops = doc.get("recommended_crops") as? List<String>,
                        timestamp = doc.getLong("timestamp")
                    )

                    records.add(record)
                }

                adapter.notifyDataSetChanged()
            }
    }
}