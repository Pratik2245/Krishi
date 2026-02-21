package com.example.krishisetuapp
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.appcompat.content.res.AppCompatResources
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfReportGenerator(private val context: Context) {

    fun generateReport(): Boolean {
        try {
            val pdfDocument = PdfDocument()
            val paint = Paint()
            val textPaint = Paint()

            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Draw border
            val borderPaint = Paint()
            borderPaint.style = Paint.Style.STROKE
            borderPaint.color = Color.BLACK
            borderPaint.strokeWidth = 2f
            canvas.drawRect(20f, 20f, 575f, 822f, borderPaint)

            // ---------- LOGO ----------
            val logoDrawable = AppCompatResources.getDrawable(context, R.drawable.logo)
            val logoBitmap = (logoDrawable as BitmapDrawable).bitmap

            val logoWidth = 80f
            val logoHeight = 80f
            val logoX = 575f - logoWidth - 10f // right border - logo width - padding
            val logoY = 30f // top padding
            val destRect = RectF(logoX, logoY, logoX + logoWidth, logoY + logoHeight)
            paint.isFilterBitmap = true // To improve scaling quality
            canvas.drawBitmap(logoBitmap, null, destRect, paint)

            // ---------- TITLE ----------
            textPaint.textSize = 22f
            textPaint.isFakeBoldText = true
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("KrishiSetu", 297f, 50f, textPaint)

            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize = 12f
            textPaint.isFakeBoldText = false

            var y = 120f

            val dateTime = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())

            canvas.drawText("Name: ____________________", 40f, y, textPaint)
            y += 20
            canvas.drawText("Location: ____________________", 40f, y, textPaint)
            y += 20
            canvas.drawText("Date & Time: $dateTime", 40f, y, textPaint)
            y += 20
            canvas.drawText("No. of Samples: 5", 40f, y, textPaint)

            // ---------- TABLE ----------
            y += 30

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = Color.BLACK

            val startX = 40f
            val startY = y
            val rowHeight = 35f
            val colWidths = floatArrayOf(60f, 80f, 60f, 80f, 70f, 85f, 90f)

            val headers = arrayOf(
                "Sample",
                "Predicted NPK",
                "pH",
                "Soil Type",
                "Moisture",
                "Temperature",
                "Crops"
            )

            var currentX: Float
            var currentY = startY

            // Draw Header Row
            currentX = startX
            for (i in headers.indices) {
                canvas.drawRect(
                    currentX,
                    currentY,
                    currentX + colWidths[i],
                    currentY + rowHeight,
                    paint
                )
                canvas.drawText(headers[i], currentX + 5, currentY + 23, textPaint)
                currentX += colWidths[i]
            }

            // Draw Data Rows (5 Samples)
            for (row in 1..5) {
                currentY += rowHeight
                currentX = startX

                val rowData = arrayOf(
                    row.toString(),
                    "120-46-60",
                    "6.8",
                    "Loamy",
                    "45%",
                    "28°C",
                    "Wheat, Rice"
                )

                for (i in rowData.indices) {
                    canvas.drawRect(
                        currentX,
                        currentY,
                        currentX + colWidths[i],
                        currentY + rowHeight,
                        paint
                    )
                    canvas.drawText(rowData[i], currentX + 5, currentY + 23, textPaint)
                    currentX += colWidths[i]
                }
            }

            // ---------- AVERAGES ----------
            currentY += rowHeight + 30

            canvas.drawText("Average of NPK: 120-46-60", 40f, currentY, textPaint)
            currentY += 18
            canvas.drawText("Average of pH: 6.8", 40f, currentY, textPaint)
            currentY += 18
            canvas.drawText("Average of Soil Temperature: 28°C", 40f, currentY, textPaint)
            currentY += 18
            canvas.drawText("Average of Soil Moisture: 45%", 40f, currentY, textPaint)

            pdfDocument.finishPage(page)

            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "KrishiSetu"
            )
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, "Soil_Report_${System.currentTimeMillis()}.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}