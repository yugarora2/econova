package com.econova.econova.data

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

/**
 * Saves/loads captured plant photos as JPEGs in internal storage,
 * keyed by plant ID, so they survive app restarts.
 */
object PlantImageStore {
    private const val DIR_NAME = "captured_plants"

    private fun dir(context: Context): File =
        File(context.filesDir, DIR_NAME).apply { if (!exists()) mkdirs() }

    fun saveImage(context: Context, plantId: String, bitmap: Bitmap): String {
        val file = File(dir(context), "$plantId.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }

    /** Called on startup to find any images already saved from a previous session. */
    fun existingImagePaths(context: Context): Map<String, String> =
        dir(context).listFiles { f -> f.extension == "jpg" }
            ?.associate { it.nameWithoutExtension to it.absolutePath }
            ?: emptyMap()
    fun deleteImage(context: Context, plantId: String) {
        val file = File(dir(context), "$plantId.jpg")
        if (file.exists()) file.delete()
    }
}

