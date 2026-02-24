package com.example.krishisetuapp.db



import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class SoilHealthReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val avgN = intent.getFloatExtra("avgN", 0f)
        val avgP = intent.getFloatExtra("avgP", 0f)
        val avgK = intent.getFloatExtra("avgK", 0f)

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 40, 40, 40)
        layout.setBackgroundColor(Color.parseColor("#F1FFF1"))
        scrollView.addView(layout)

        val title = TextView(this)
        title.text = "🌾 Soil Health Report"
        title.textSize = 22f
        title.setTextColor(Color.parseColor("#1B5E20"))
        title.gravity = Gravity.CENTER
        title.setPadding(0, 0, 0, 30)
        layout.addView(title)

        // Calculate Score
        val scoreN = calculateScore(avgN, 40f, 80f)
        val scoreP = calculateScore(avgP, 20f, 40f)
        val scoreK = calculateScore(avgK, 20f, 50f)

        val totalScore = scoreN + scoreP + scoreK

        val healthStatus = when {
            totalScore >= 80 -> "Excellent 🌟"
            totalScore >= 60 -> "Good 🌿"
            totalScore >= 40 -> "Moderate 🌱"
            else -> "Poor ⚠"
        }

        val card = CardView(this)
        card.radius = 20f
        card.setCardBackgroundColor(Color.WHITE)
        card.setContentPadding(30, 30, 30, 30)
        card.cardElevation = 10f

        val text = TextView(this)
        text.textSize = 16f
        text.setTextColor(Color.DKGRAY)

        text.text = """
📊 Average Values:
N: %.2f
P: %.2f
K: %.2f

🌿 Soil Health Score:
$totalScore / 100

Status: $healthStatus

----------------------------
📌 Reference Ideal Ranges:
Nitrogen: 40 - 80
Phosphorus: 20 - 40
Potassium: 20 - 50
        """.trimIndent().format(avgN, avgP, avgK)

        card.addView(text)
        layout.addView(card)

        setContentView(scrollView)
    }

    private fun calculateScore(value: Float, min: Float, max: Float): Int {
        return when {
            value in min..max -> 33
            value in (min - 10)..(max + 10) -> 20
            else -> 10
        }
    }
}