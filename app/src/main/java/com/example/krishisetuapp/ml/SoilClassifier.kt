package com.example.krishisetuapp.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class SoilClassifier(context: Context) {

    private var interpreter: Interpreter
    private val inputSize = 224   // Teachable Machine default

    init {
        val assetFile = context.assets.openFd("model_unquant.tflite")
        val inputStream = FileInputStream(assetFile.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFile.startOffset
        val declaredLength = assetFile.declaredLength
        val model = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

        interpreter = Interpreter(model)
    }

    fun isSoil(bitmap: Bitmap): Boolean {

        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        val inputBuffer =
            ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        inputBuffer.order(ByteOrder.nativeOrder())

        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val pixel = resized.getPixel(x, y)

                val r = ((pixel shr 16) and 0xFF) / 255f
                val g = ((pixel shr 8) and 0xFF) / 255f
                val b = (pixel and 0xFF) / 255f

                inputBuffer.putFloat(r)
                inputBuffer.putFloat(g)
                inputBuffer.putFloat(b)
            }
        }

        val output = Array(1) { FloatArray(2) }

        interpreter.run(inputBuffer, output)

        val soilConfidence = output[0][0]
        val notSoilConfidence = output[0][1]

        return soilConfidence > notSoilConfidence
    }
}
