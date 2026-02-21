package com.example.krishisetuapp.db
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.krishisetuapp.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class NpkHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Root Layout
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 30, 30, 30)

        val textView = TextView(this)
        textView.textSize = 16f

        val uploadButton = Button(this)
        uploadButton.text = "Upload To Firebase"

        layout.addView(textView)
        layout.addView(uploadButton)

        setContentView(layout)

        // 🔥 Back always goes to Dashboard
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
                mapOf(
                    "N" to n,
                    "P" to p,
                    "K" to k
                )
            )

            builder.append("Sample ${i + 1}\n")
            builder.append("N: $n  P: $p  K: $k\n\n")
        }

        var avgN = 0f
        var avgP = 0f
        var avgK = 0f

        if (jsonArray.length() > 0) {

            avgN = totalN / jsonArray.length()
            avgP = totalP / jsonArray.length()
            avgK = totalK / jsonArray.length()

            builder.append("-------- AVERAGE --------\n")
            builder.append("Avg N: %.2f\n".format(avgN))
            builder.append("Avg P: %.2f\n".format(avgP))
            builder.append("Avg K: %.2f\n".format(avgK))

        } else {
            builder.append("No samples available.")
            uploadButton.isEnabled = false
        }

        textView.text = builder.toString()

        // 🔥 Upload only when button clicked
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

                    Toast.makeText(
                        this,
                        "Uploaded successfully & local data cleared",
                        Toast.LENGTH_SHORT
                    ).show()

                    // 🔥 Clear SharedPreferences
                    prefs.edit()
                        .putString("samples", "[]")
                        .apply()

                    uploadButton.isEnabled = false
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Upload failed: ${it.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}