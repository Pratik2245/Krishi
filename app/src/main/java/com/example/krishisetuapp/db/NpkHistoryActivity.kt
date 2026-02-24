package com.example.krishisetuapp.db

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.krishisetuapp.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class NpkHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Root ScrollView
        val scrollView = ScrollView(this)
        val rootLayout = LinearLayout(this)
        rootLayout.orientation = LinearLayout.VERTICAL
        rootLayout.setPadding(40, 40, 40, 40)
        rootLayout.setBackgroundColor(Color.parseColor("#F1FFF1"))
        scrollView.addView(rootLayout)

        // Title
        val title = TextView(this)
        title.text = "🌾 KrishiSetu NPK History"
        title.textSize = 22f
        title.setTextColor(Color.parseColor("#1B5E20"))
        title.gravity = Gravity.CENTER
        title.setPadding(0, 0, 0, 30)
        rootLayout.addView(title)

        // Card for Data
        val card = CardView(this)
        card.radius = 20f
        card.setCardBackgroundColor(Color.WHITE)
        card.setContentPadding(30, 30, 30, 30)
        card.cardElevation = 12f

        val dataText = TextView(this)
        dataText.textSize = 16f
        dataText.setTextColor(Color.DKGRAY)
        card.addView(dataText)
        rootLayout.addView(card)

        // Spacer
        val space = Space(this)
        space.minimumHeight = 40
        rootLayout.addView(space)

        // Buttons Layout
        val buttonLayout = LinearLayout(this)
        buttonLayout.orientation = LinearLayout.VERTICAL
        buttonLayout.gravity = Gravity.CENTER
        buttonLayout.setPadding(0, 20, 0, 0)

        // Upload Button
        val uploadButton = Button(this)
        uploadButton.text = "☁ Upload To Firebase"
        uploadButton.setBackgroundColor(Color.parseColor("#2E7D32"))
        uploadButton.setTextColor(Color.WHITE)
        uploadButton.textSize = 16f

        // Soil Health Button
        val healthButton = Button(this)
        healthButton.text = "🌿 View Soil Health Report"
        healthButton.setBackgroundColor(Color.parseColor("#388E3C"))
        healthButton.setTextColor(Color.WHITE)
        healthButton.textSize = 16f

        buttonLayout.addView(uploadButton)
        buttonLayout.addView(Space(this).apply { minimumHeight = 20 })
        buttonLayout.addView(healthButton)

        rootLayout.addView(buttonLayout)

        setContentView(scrollView)

        // Back to Dashboard
        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val intent = Intent(this@NpkHistoryActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                }
            })

        val prefs = getSharedPreferences("npk_prefs", MODE_PRIVATE)
        val data = prefs.getString("samples", "[]") ?: "[]"
        val jsonArray = JSONArray(data)

        var totalN = 0f
        var totalP = 0f
        var totalK = 0f

        val builder = StringBuilder()
        val sampleList = mutableListOf<Map<String, Float>>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            val n = obj.getDouble("N").toFloat()
            val p = obj.getDouble("P").toFloat()
            val k = obj.getDouble("K").toFloat()

            totalN += n
            totalP += p
            totalK += k

            sampleList.add(
                mapOf("N" to n, "P" to p, "K" to k)
            )

            builder.append("🌱 Sample ${i + 1}\n")
            builder.append("   N: $n   P: $p   K: $k\n\n")
        }

        var avgN = 0f
        var avgP = 0f
        var avgK = 0f

        if (jsonArray.length() > 0) {

            avgN = totalN / jsonArray.length()
            avgP = totalP / jsonArray.length()
            avgK = totalK / jsonArray.length()

            builder.append("━━━━━━━━━━━━━━━━━━\n")
            builder.append("🌿 AVERAGE VALUES\n")
            builder.append("Avg N: %.2f\n".format(avgN))
            builder.append("Avg P: %.2f\n".format(avgP))
            builder.append("Avg K: %.2f\n".format(avgK))

        } else {
            builder.append("No samples available.")
            uploadButton.isEnabled = false
            healthButton.isEnabled = false
        }

        dataText.text = builder.toString()

        // Upload Button Logic (UNCHANGED)
        uploadButton.setOnClickListener {

            if (jsonArray.length() == 0) {
                Toast.makeText(this, "No data to upload", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val currentDate = dateFormat.format(Date())

            val uploadData = hashMapOf(
                "user_id" to userId,
                "date" to currentDate,
                "total_samples" to jsonArray.length(),
                "samples" to sampleList,
                "average_N" to avgN,
                "average_P" to avgP,
                "average_K" to avgK,
                "created_at" to System.currentTimeMillis()
            )

            db.collection("npk_average_records")
                .add(uploadData)
                .addOnSuccessListener {
                    Toast.makeText(this,
                        "Uploaded successfully & local data cleared",
                        Toast.LENGTH_SHORT).show()

                    prefs.edit().putString("samples", "[]").apply()
                    uploadButton.isEnabled = false
                    healthButton.isEnabled = false
                }
                .addOnFailureListener {
                    Toast.makeText(this,
                        "Upload failed: ${it.message}",
                        Toast.LENGTH_LONG).show()
                }
        }

        // Soil Health Navigation
        healthButton.setOnClickListener {
            val intent = Intent(this, SoilHealthReportActivity::class.java)
            intent.putExtra("avgN", avgN)
            intent.putExtra("avgP", avgP)
            intent.putExtra("avgK", avgK)
            startActivity(intent)
        }
    }
}