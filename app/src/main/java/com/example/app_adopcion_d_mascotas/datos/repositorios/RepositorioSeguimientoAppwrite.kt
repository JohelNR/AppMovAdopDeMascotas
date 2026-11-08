package com.example.app_adopcion_d_mascotas.datos.repositorios

import com.example.app_adopcion_d_mascotas.datos.modelos.SeguimientoMascota
import com.example.app_adopcion_d_mascotas.datos.remoto.ClienteAppwrite
import io.appwrite.ID
import io.appwrite.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
 * ============================================================================
 * ARCHIVO: RepositorioSeguimientoAppwrite.kt
 * CAPA: Datos -> Repositorios (RF11)
 * ¿QUÉ HACE?: Guarda y consulta los reportes fotográficos de seguimiento
 *             post-adopción directamente en la tabla "seguimientos" de
 *             Appwrite Cloud (100% en la nube, cero memoria local falsa).
 *             Columnas en español: idMascota, nombreMascota, idSolicitante,
 *             nombreSolicitante, nombreRefugio, fecha, notasSalud,
 *             notasAdaptacion, fotos, estado.
 * ============================================================================
 */
object RepositorioSeguimientoAppwrite {

    /*
     * RF11: Guardar un nuevo reporte de seguimiento post-adopción en la tabla "seguimientos".
     */
    suspend fun crearSeguimiento(seguimiento: SeguimientoMascota): Boolean = withContext(Dispatchers.IO) {
        try {
            // Paso 1: Empaquetar los datos con los nombres de columna en español
            val datosSeguimiento = mapOf(
                "idMascota" to seguimiento.idMascota,
                "nombreMascota" to seguimiento.nombreMascota,
                "idSolicitante" to seguimiento.idSolicitante,
                "nombreSolicitante" to seguimiento.nombreSolicitante,
                "nombreRefugio" to seguimiento.nombreRefugio,
                "fecha" to seguimiento.fecha,
                "notasSalud" to seguimiento.notasSalud.trim(),
                "notasAdaptacion" to seguimiento.notasAdaptacion.trim(),
                "fotos" to seguimiento.fotos,
                "estado" to seguimiento.estado
            )

            // Paso 2: Crear el documento real en Appwrite Cloud
            ClienteAppwrite.baseDeDatos.createDocument(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_SEGUIMIENTOS,
                documentId = ID.unique(),
                data = datosSeguimiento
            )
            true
        } catch (error: Exception) {
            error.printStackTrace()
            false
        }
    }

    suspend fun createFollowUp(followUp: SeguimientoMascota): Boolean = crearSeguimiento(followUp)

    /*
     * RF11: Obtener todos los reportes de seguimiento de una mascota desde Appwrite Cloud.
     */
    suspend fun obtenerSeguimientos(idMascota: String): List<SeguimientoMascota> = withContext(Dispatchers.IO) {
        try {
            // Paso 1: Consultar la tabla "seguimientos" filtrando por "idMascota"
            val respuesta = ClienteAppwrite.baseDeDatos.listDocuments(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_SEGUIMIENTOS,
                queries = listOf(
                    Query.equal("idMascota", idMascota),
                    Query.orderDesc("\$createdAt")
                )
            )

            // Paso 2: Transformar cada fila de la nube en un objeto SeguimientoMascota
            respuesta.documents.map { documento ->
                val datos = documento.data
                val listaFotos = (datos["fotos"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

                SeguimientoMascota(
                    id = documento.id,
                    petId = datos["idMascota"]?.toString() ?: "",
                    petName = datos["nombreMascota"]?.toString() ?: "",
                    applicantId = datos["idSolicitante"]?.toString() ?: "",
                    applicantName = datos["nombreSolicitante"]?.toString() ?: "",
                    shelterName = datos["nombreRefugio"]?.toString() ?: "",
                    date = datos["fecha"]?.toString() ?: "",
                    healthNotes = datos["notasSalud"]?.toString() ?: "",
                    adaptationNotes = datos["notasAdaptacion"]?.toString() ?: "",
                    photos = listaFotos,
                    status = datos["estado"]?.toString() ?: "ENVIADO"
                )
            }
        } catch (error: Exception) {
            error.printStackTrace()
            emptyList()
        }
    }

    suspend fun getFollowUps(petId: String): List<SeguimientoMascota> = obtenerSeguimientos(petId)

    /*
     * Permite al Refugio marcar un reporte de seguimiento como "REVISADO" en la nube.
     */
    suspend fun marcarComoRevisado(idSeguimiento: String): Boolean = withContext(Dispatchers.IO) {
        try {
            ClienteAppwrite.baseDeDatos.updateDocument(
                databaseId = ClienteAppwrite.ID_BASE_DATOS,
                collectionId = ClienteAppwrite.TABLA_SEGUIMIENTOS,
                documentId = idSeguimiento,
                data = mapOf("estado" to "REVISADO")
            )
            true
        } catch (error: Exception) {
            error.printStackTrace()
            false
        }
    }
}

typealias AppwriteFollowUpRepository = RepositorioSeguimientoAppwrite
