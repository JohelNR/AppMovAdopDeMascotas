package com.example.app_adopcion_d_mascotas.data.repository

import android.content.Context
import android.net.Uri
import com.example.app_adopcion_d_mascotas.data.remote.AppwriteClient
import io.appwrite.ID
import io.appwrite.models.InputFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppwriteStorageRepository {
    suspend fun uploadPhoto(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext null
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val fileName = "pet_${System.currentTimeMillis()}.jpg"

            val inputFile = InputFile.fromBytes(bytes, fileName, mimeType)
            val result = AppwriteClient.storage.createFile(
                bucketId = AppwriteClient.BUCKET_ID,
                fileId = ID.unique(),
                file = inputFile
            )
            result.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
