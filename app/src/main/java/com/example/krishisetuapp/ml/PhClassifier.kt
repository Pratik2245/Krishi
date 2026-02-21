package com.example.krishisetuapp.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class PhClassifier(context: Context) {

    private var interpreter: Interpreter

    init {
        val fileDescriptor = context.assets.openFd("ph_model_unquant.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val model = fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            startOffset,
            declaredLength
        )
        interpreter = Interpreter(model)
    }

    fun isPhPaper(bitmap: Bitmap): Boolean {

        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        val inputBuffer =
            ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        inputBuffer.order(ByteOrder.nativeOrder())

        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = resized.getPixel(x, y)

                inputBuffer.putFloat(((pixel shr 16) and 0xFF) / 255f)
                inputBuffer.putFloat(((pixel shr 8) and 0xFF) / 255f)
                inputBuffer.putFloat((pixel and 0xFF) / 255f)
            }
        }

        val output = Array(1) { FloatArray(2) }

        interpreter.run(inputBuffer, output)

        val phScore = output[0][0]      // 0 = PH
        val nonPhScore = output[0][1]   // 1 = NON PH

        return phScore > nonPhScore
    }
}