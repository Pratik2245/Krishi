package com.example.krishisetuapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.krishisetuapp.adapter.Farmer
import com.example.krishisetuapp.adapter.FarmerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FarmerAdapter
    private val db = FirebaseFirestore.getInstance()

    private val farmerList = mutableListOf<Farmer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Admin Dashboard"
        recyclerView = findViewById(R.id.recyclerFarmers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = FarmerAdapter(farmerList) { userId ->
            val intent = Intent(this, FarmerDetailsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        loadFarmers()

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadFarmers() {

        db.collection("soil_records")
            .get()
            .addOnSuccessListener { result ->

                val uniqueUserIds = result.documents.mapNotNull {
                    it.getString("user_id")
                }.toSet()

                farmerList.clear()

                for (userId in uniqueUserIds) {

                    db.collection("users")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { userDoc ->

                            val name = userDoc.getString("email") ?: "Unknown Farmer"

                            farmerList.add(Farmer(userId, name))
                            adapter.notifyDataSetChanged()
                        }
                }
            }
    }
}