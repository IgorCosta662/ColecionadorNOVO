package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageStorageHelper {

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, prefix: String = "item"): String {
        val imagesDir = File(context.filesDir, "item_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val filename = "${prefix}_${UUID.randomUUID()}.jpg"
        val file = File(imagesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
        }
        return Uri.fromFile(file).toString()
    }

    fun saveBase64ToInternalStorage(context: Context, base64: String, prefix: String = "scan"): String {
        val cleanBase64 = if (base64.contains(",")) base64.substringAfter(",") else base64
        val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw IllegalArgumentException("Bitmap não pôde ser decodificado do Base64")
        return saveBitmapToInternalStorage(context, bitmap, prefix)
    }

    fun loadBitmapFromUri(context: Context, uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file") {
                BitmapFactory.decodeFile(uri.path)
            } else {
                val input: InputStream? = context.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(input)
            }
        } catch (_: Exception) {
            null
        }
    }
}
