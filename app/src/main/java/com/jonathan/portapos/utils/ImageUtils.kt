package com.jonathan.portapos.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.*

object ImageUtils {

    /**
     * Copies an image from a URI to the app's internal storage.
     * Returns the absolute path to the saved file.
     */
    fun saveImageToInternalStorage(context: Context, uri: Uri, prefix: String = "img"): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            
            val fileName = "${prefix}_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)
            
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletes a file at the given path if it exists.
     */
    fun deleteImageFile(path: String?) {
        if (path != null) {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}
