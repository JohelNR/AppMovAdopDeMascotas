package com.example.app_adopcion_d_mascotas.datos.repositorios

import android.content.Context
import android.net.Uri
import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite
import io.appwrite.ID
import io.appwrite.models.InputFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
 * ============================================================================
 * ARCHIVO: RepositorioAlmacenamientoAppwrite.kt
 * CAPA: Datos -> Repositorios (RF2, RF3, RF11)
 * ¿QUÉ HACE?: Sube archivos de imagen (fotos de mascotas, fotos de perfil y
 *             evidencias de seguimiento) al bucket "fotos_mascotas" en
 *             Appwrite Storage y devuelve el ID del archivo en la nube.
 * ============================================================================
 */
object RepositorioAlmacenamientoAppwrite {

    /*
     * Sube una imagen desde una URI del teléfono (galería o foto ya recortada)
     * hacia el bucket "fotos_mascotas" en Appwrite Cloud.
     */
    suspend fun subirFoto(contexto: Context, uriImagen: Uri): String? = withContext(Dispatchers.IO) {
        try {
            // Paso 1: Leer los bytes de la imagen desde la memoria del dispositivo
            val lectorContenido = contexto.contentResolver
            val bytesImagen = lectorContenido.openInputStream(uriImagen)?.use { it.readBytes() }
                ?: return@withContext null

            // Paso 2: Preparar el archivo con nombre único y formato JPEG
            val tipoMime = lectorContenido.getType(uriImagen) ?: "image/jpeg"
            val nombreArchivo = "foto_${System.currentTimeMillis()}.jpg"
            val archivoEntrada = InputFile.fromBytes(bytesImagen, nombreArchivo, tipoMime)

            // Paso 3: Subir el archivo al bucket "fotos_mascotas" de Appwrite Cloud
            val archivoSubido = ClienteAppwrite.almacenamiento.createFile(
                bucketId = ClienteAppwrite.ID_BUCKET_FOTOS,
                fileId = ID.unique(),
                file = archivoEntrada
            )

            // Paso 4: Devolver el ID único asignado por Appwrite
            archivoSubido.id
        } catch (error: Exception) {
            error.printStackTrace()
            null
        }
    }

    suspend fun uploadPhoto(context: Context, uri: Uri): String? = subirFoto(context, uri)
}

typealias AppwriteStorageRepository = RepositorioAlmacenamientoAppwrite
