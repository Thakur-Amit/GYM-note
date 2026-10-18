package com.example.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class ImageStorageManager(private val context: Context) {

    private val profileDir: File
        get() = File(context.filesDir, "profile_images").apply { if (!exists()) mkdirs() }

    private val exerciseDir: File
        get() = File(context.filesDir, "exercise_images").apply { if (!exists()) mkdirs() }

    /**
     * Copies and optimizes a picked image URI into internal app storage for profile photo.
     * Returns the absolute path of the saved file, or null on failure.
     */
    fun saveProfilePicture(sourceUri: Uri, oldPath: String? = null): String? {
        return try {
            // Remove previous profile photo if exists
            oldPath?.let { deleteFile(it) }

            val targetFile = File(profileDir, "profile_${System.currentTimeMillis()}.jpg")
            compressAndCopy(sourceUri, targetFile, maxDimension = 720)
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletes a profile photo file.
     */
    fun removeProfilePicture(path: String?): Boolean {
        return path?.let { deleteFile(it) } ?: false
    }

    /**
     * Copies and optimizes a picked image URI into internal app storage for an exercise.
     * Returns the absolute path of the saved file.
     */
    fun saveExerciseImage(sourceUri: Uri, exerciseId: String, oldPath: String? = null): String? {
        return try {
            oldPath?.let { if (!it.startsWith("res:")) deleteFile(it) }

            val cleanId = exerciseId.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val targetFile = File(exerciseDir, "ex_${cleanId}_${UUID.randomUUID().toString().take(6)}.jpg")
            compressAndCopy(sourceUri, targetFile, maxDimension = 1080)
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun removeExerciseImage(path: String?): Boolean {
        if (path == null || path.startsWith("res:")) return false
        return deleteFile(path)
    }

    private fun deleteFile(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    private fun compressAndCopy(sourceUri: Uri, destination: File, maxDimension: Int) {
        val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (bitmap == null) {
            throw IllegalArgumentException("Unable to decode bitmap from URI")
        }

        // Scale down if larger than maxDimension
        val width = bitmap.width
        val height = bitmap.height
        val scaledBitmap = if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            val newWidth: Int
            val newHeight: Int
            if (ratio > 1) {
                newWidth = maxDimension
                newHeight = (maxDimension / ratio).toInt()
            } else {
                newHeight = maxDimension
                newWidth = (maxDimension * ratio).toInt()
            }
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }

        FileOutputStream(destination).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
        }

        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        bitmap.recycle()
    }
}
