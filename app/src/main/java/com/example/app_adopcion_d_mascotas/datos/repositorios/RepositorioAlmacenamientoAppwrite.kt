package com.example.app_adopcion_d_mascotas.datos.repositorios

import android.content.Context
import android.net.Uri
import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite
import io.appwrite.ID
import io.appwrite.models.InputFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RepositorioAlmacenamientoAppwrite {

    suspend fun uploadPhoto(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext null
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val fileName = "pet_${System.currentTimeMillis()}.jpg"

            val inputFile = InputFile.fromBytes(bytes, fileName, mimeType)
            val result = ClienteAppwrite.storage.createFile(
                bucketId = ClienteAppwrite.BUCKET_ID,
                fileId = ID.unique(),
                file = inputFile
            )
            result.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun subirFoto(contexto: Context, uri: Uri): String? = uploadPhoto(contexto, uri)
}

typealias AppwriteStorageRepository = RepositorioAlmacenamientoAppwrite
