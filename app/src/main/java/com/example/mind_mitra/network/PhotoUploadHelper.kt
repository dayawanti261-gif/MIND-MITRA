package com.example.mind_mitra.network

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

/** Helpers for multipart photo upload to POST /photos/upload. */
object PhotoUploadHelper {

    fun textPart(value: String): RequestBody =
        value.toRequestBody("text/plain".toMediaTypeOrNull())

    fun imagePart(file: File, partName: String = "file"): MultipartBody.Part {
        val mime = when (file.extension.lowercase()) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
        val body = file.asRequestBody(mime.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, body)
    }

    suspend fun uploadPhoto(
        context: Context,
        userId: String,
        title: String,
        description: String,
        category: String,
        uri: Uri
    ): PhotoUploadResponse {
        val file = copyUriToCacheFile(context, uri)
        return RetrofitClient.apiService.uploadPhoto(
            userId = textPart(userId),
            title = textPart(title),
            description = textPart(description),
            people = textPart(""),
            category = textPart(category),
            file = imagePart(file)
        )
    }

    private fun copyUriToCacheFile(context: Context, uri: Uri): File {
        val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
        val ext = when {
            mime.contains("png") -> "png"
            mime.contains("webp") -> "webp"
            else -> "jpg"
        }
        val file = File(context.cacheDir, "memory_upload_${System.currentTimeMillis()}.$ext")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        } ?: throw IllegalStateException("Unable to read selected image.")
        return file
    }
}
