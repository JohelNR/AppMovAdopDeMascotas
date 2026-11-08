package com.example.app_adopcion_d_mascotas.datos.repositorios

import com.example.app_adopcion_d_mascotas.datos.modelos.EstadoMascota
import com.example.app_adopcion_d_mascotas.datos.modelos.Mascota
import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.models.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
 * ============================================================================
 * ARCHIVO: RepositorioMascotasAppwrite.kt
 * CAPA: Datos -> Repositorios (RF3, RF4, RF5, RF6, RF7)
 * ¿QUÉ HACE?: Conecta la aplicación con la tabla "mascotas" en Appwrite Cloud.
 *             Todas las columnas están 100% en español:
 *             nombre, especie, raza, edadAnios, sexo, tamano, descripcion,
 *             nombreRefugio, idRefugio, ubicacion, latitud, longitud,
 *             esUrgente, estado, fotos.
 * ============================================================================
 */
object RepositorioMascotasAppwrite {

    /*
     * RF4: Obtener la lista completa de mascotas desde la nube, ordenadas de la más reciente a la más antigua.
     */
    suspend fun obtenerMascotas(): List<Mascota> = withContext(Dispatchers.IO) {
        try {
            // Paso 1: Pedir todos los documentos de la tabla "mascotas" en Appwrite
            val respuesta = ClienteAppwrite.baseDeDatos.listDocuments(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_MASCOTAS,
                queries = listOf(Query.orderDesc("\$createdAt"))
            )

            // Paso 2: Convertir cada documento de la nube en un objeto Mascota de Kotlin
            respuesta.documents.map { documentoAMascota(it) }
        } catch (error: Exception) {
            error.printStackTrace()
            emptyList()
        }
    }

    suspend fun getPets(): List<Mascota> = obtenerMascotas()

    /*
     * Convierte un objeto Mascota al mapa de columnas en español de la tabla "mascotas"
     */
    private fun convertirMascotaAMapa(mascota: Mascota): Map<String, Any> = mapOf(
        "nombre" to mascota.nombre.trim(),
        "especie" to mascota.especie,
        "raza" to mascota.raza.trim(),
        "edadAnios" to mascota.edadAnios,
        "sexo" to mascota.sexo,
        "tamano" to mascota.tamano,
        "descripcion" to mascota.descripcion.trim(),
        "nombreRefugio" to mascota.nombreRefugio.trim(),
        "idRefugio" to mascota.idRefugio,
        "ubicacion" to mascota.ubicacion.trim(),
        "latitud" to mascota.latitud,
        "longitud" to mascota.longitud,
        "esUrgente" to mascota.esUrgente,
        "estado" to mascota.estado.name,
        "fotos" to mascota.fotos
    )

    /*
     * RF3 y RF7: Crear una nueva mascota en la tabla "mascotas" de Appwrite Cloud.
     */
    suspend fun crearMascota(mascota: Mascota): Boolean = withContext(Dispatchers.IO) {
        try {
            // Paso 1: Guardar el documento en Appwrite Cloud con sus columnas en español
            ClienteAppwrite.baseDeDatos.createDocument(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_MASCOTAS,
                documentId = ID.unique(),
                data = convertirMascotaAMapa(mascota)
            )
            true
        } catch (error: Exception) {
            error.printStackTrace()
            false
        }
    }

    suspend fun createPet(pet: Mascota): Boolean = crearMascota(pet)

    /*
     * RF6: Actualizar todos los datos de una mascota existente en la nube.
     */
    suspend fun actualizarMascota(mascota: Mascota): Boolean = withContext(Dispatchers.IO) {
        try {
            ClienteAppwrite.baseDeDatos.updateDocument(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_MASCOTAS,
                documentId = mascota.id,
                data = convertirMascotaAMapa(mascota)
            )
            true
        } catch (error: Exception) {
            error.printStackTrace()
            false
        }
    }

    suspend fun updatePet(pet: Mascota): Boolean = actualizarMascota(pet)

    /*
     * RF6: Eliminar una mascota publicada de la tabla "mascotas".
     */
    suspend fun eliminarMascota(idMascota: String): Boolean = withContext(Dispatchers.IO) {
        try {
            ClienteAppwrite.baseDeDatos.deleteDocument(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_MASCOTAS,
                documentId = idMascota
            )
            true
        } catch (error: Exception) {
            error.printStackTrace()
            false
        }
    }

    suspend fun deletePet(petId: String): Boolean = eliminarMascota(petId)

    /*
     * RF6 y RF10: Cambiar el estado de una mascota (DISPONIBLE, EN_PROCESO o ADOPTADA).
     */
    suspend fun cambiarEstadoMascota(idMascota: String, nuevoEstado: EstadoMascota): Boolean = withContext(Dispatchers.IO) {
        try {
            ClienteAppwrite.baseDeDatos.updateDocument(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_MASCOTAS,
                documentId = idMascota,
                data = mapOf("estado" to nuevoEstado.name)
            )
            true
        } catch (error: Exception) {
            error.printStackTrace()
            false
        }
    }

    suspend fun marcarComoAdoptada(idMascota: String): Boolean =
        cambiarEstadoMascota(idMascota, EstadoMascota.ADOPTADA)

    suspend fun markAsAdopted(petId: String): Boolean = marcarComoAdoptada(petId)

    /*
     * Función auxiliar que traduce un documento de Appwrite Cloud (en español) al modelo Mascota.
     */
    private fun documentoAMascota(documento: Document<Map<String, Any>>): Mascota {
        val datos = documento.data
        val listaFotos = (datos["fotos"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        val textoEstado = datos["estado"]?.toString() ?: "DISPONIBLE"
        val estadoMascota = try {
            EstadoMascota.valueOf(textoEstado)
        } catch (_: Exception) {
            EstadoMascota.DISPONIBLE
        }

        return Mascota(
            id = documento.id,
            name = datos["nombre"]?.toString() ?: "",
            species = datos["especie"]?.toString() ?: "Perro",
            breed = datos["raza"]?.toString() ?: "",
            ageYears = (datos["edadAnios"] as? Number)?.toInt() ?: 1,
            gender = datos["sexo"]?.toString() ?: "Macho",
            size = datos["tamano"]?.toString() ?: "Mediano",
            description = datos["descripcion"]?.toString() ?: "",
            shelterName = datos["nombreRefugio"]?.toString() ?: "",
            shelterId = datos["idRefugio"]?.toString() ?: "",
            location = datos["ubicacion"]?.toString() ?: "Cajamarca",
            latitude = (datos["latitud"] as? Number)?.toDouble() ?: -7.1617,
            longitude = (datos["longitud"] as? Number)?.toDouble() ?: -78.5128,
            isUrgent = datos["esUrgente"] as? Boolean ?: false,
            status = estadoMascota,
            photos = listaFotos
        )
    }
}

typealias AppwritePetRepository = RepositorioMascotasAppwrite
