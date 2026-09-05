package com.econova.econova.logic

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class PlantScanner(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var inputSize: Int = 640

    companion object {
        private const val MODEL_FILE = "best_w8a32.tflite"
        private const val CONFIDENCE_THRESHOLD = 0.5f

        // Index order matches Roboflow's alphabetical class list,
        // which matches PlantRepository's plant IDs 1-9 in order.
        val LABELS = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9")
    }

    init {
        try {
            val model = loadModelFile(context, MODEL_FILE)
            interpreter = Interpreter(model)

            val inputShape = interpreter!!.getInputTensor(0).shape()
            inputSize = inputShape[1]

        } catch (e: Exception) {
            Log.e("PlantScanner", "Error loading model", e)
        }
    }

    private fun loadModelFile(
        context: Context,
        modelName: String
    ): ByteBuffer {

        val fileDescriptor = context.assets.openFd(modelName)

        val inputStream =
            FileInputStream(fileDescriptor.fileDescriptor)

        val fileChannel =
            inputStream.channel

        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    /** Scans a cropped frame and returns the detected plant ID, or null if nothing confident enough. */
    fun scanFrame(bitmap: Bitmap): String? {
        val interp = interpreter ?: return null

        val resized =
            Bitmap.createScaledBitmap(
                bitmap,
                inputSize,
                inputSize,
                true
            )

        val inputBuffer = bitmapToByteBuffer(resized)

        val outputShape =
            interp.getOutputTensor(0).shape()

        val output =
            Array(outputShape[0]) {
                Array(outputShape[1]) {
                    FloatArray(outputShape[2])
                }
            }

        interp.run(inputBuffer, output)

        return parseBestDetection(
            output[0],
            outputShape[1],
            outputShape[2]
        )
    }

    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer =
            ByteBuffer.allocateDirect(
                4 * inputSize * inputSize * 3
            )

        buffer.order(ByteOrder.nativeOrder())

        val pixels =
            IntArray(inputSize * inputSize)

        bitmap.getPixels(
            pixels,
            0,
            inputSize,
            0,
            0,
            inputSize,
            inputSize
        )

        for (pixel in pixels) {
            buffer.putFloat(
                ((pixel shr 16) and 0xFF) / 255f
            )

            buffer.putFloat(
                ((pixel shr 8) and 0xFF) / 255f
            )

            buffer.putFloat(
                (pixel and 0xFF) / 255f
            )
        }

        buffer.rewind()

        return buffer
    }

    private fun parseBestDetection(
        output: Array<FloatArray>,
        rows: Int,
        numAnchors: Int
    ): String? {

        // rows 0..3 = box coords (cx, cy, w, h)
        // rows 4..end = per-class confidence

        var bestScore = 0f
        var bestClass = -1

        for (anchor in 0 until numAnchors) {
            for (classIdx in 0 until (rows - 4)) {

                val score =
                    output[4 + classIdx][anchor]

                if (score > bestScore) {
                    bestScore = score
                    bestClass = classIdx
                }
            }
        }

        if (
            bestClass == -1 ||
            bestScore < CONFIDENCE_THRESHOLD
        ) {
            return null
        }

        return LABELS.getOrNull(bestClass)
    }
}