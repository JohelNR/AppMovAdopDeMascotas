package com.example.app_adopcion_d_mascotas.datos.remoto

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Realtime
import io.appwrite.services.Storage
import io.appwrite.services.TablesDB

/*
 * ============================================================================
 * ARCHIVO: ClienteAppwrite.kt
 * CAPA: Datos -> Remoto (Conexión con la nube Appwrite Cloud)
 * ¿QUÉ HACE?: Es el "puente" único (Singleton) entre el celular y nuestro
 *             servidor en internet (Appwrite Cloud).
 *             Aquí están guardados los nombres en español de las 6 tablas.
 * ============================================================================
 */
object ClienteAppwrite {

    // 1. Dirección del servidor en la nube y código de nuestro proyecto
    const val ENDPOINT = "https://fra.cloud.appwrite.io/v1"
    const val PROJECT_ID = "6aceca44001361ba94f0"

    // 2. Nombre de la Base de Datos y del Almacén de Fotos (Bucket)
    const val ID_BASE_DATOS = "adopcion_db"
    const val ID_BUCKET_FOTOS = "fotos_mascotas"

    // 3. Nombres de las 6 Tablas en Español (Colecciones en Appwrite)
    const val TABLA_USUARIOS = "usuarios"
    const val TABLA_MASCOTAS = "mascotas"
    const val TABLA_SOLICITUDES = "solicitudes"
    const val TABLA_CITAS = "citas"
    const val TABLA_SEGUIMIENTOS = "seguimientos"
    const val TABLA_MENSAJES = "mensajes"

    // Alias para mantener compatibilidad con cualquier referencia anterior
    const val DATABASE_ID = ID_BASE_DATOS
    const val BUCKET_ID = ID_BUCKET_FOTOS
    const val USERS_COLLECTION = TABLA_USUARIOS
    const val PETS_COLLECTION = TABLA_MASCOTAS
    const val REQUESTS_COLLECTION = TABLA_SOLICITUDES
    const val APPOINTMENTS_COLLECTION = TABLA_CITAS
    const val FOLLOWUPS_COLLECTION = TABLA_SEGUIMIENTOS
    const val CHATS_COLLECTION = TABLA_MENSAJES

    // 4. Servicios oficiales de Appwrite (Cuentas, Base de Datos, Fotos y Tiempo Real)
    private lateinit var cliente: Client
    lateinit var cuenta: Account
        private set
    lateinit var baseDeDatos: Databases
        private set
    lateinit var tablasDB: TablesDB
        private set
    lateinit var almacenamiento: Storage
        private set
    lateinit var tiempoReal: Realtime
        private set

    // Alias de servicios
    val account: Account get() = cuenta
    val databases: Databases get() = baseDeDatos
    val tablesDB: TablesDB get() = tablasDB
    val storage: Storage get() = almacenamiento
    val realtime: Realtime get() = tiempoReal

    /*
     * Función que enciende la conexión con Appwrite al abrir la aplicación (en MainActivity)
     */
    fun inicializar(contexto: Context) {
        // Paso 1: Configurar el cliente con el servidor y ID del proyecto
        cliente = Client(contexto)
            .setEndpoint(ENDPOINT)
            .setProject(PROJECT_ID)

        // Paso 2: Crear cada servicio usando ese cliente conectado
        cuenta = Account(cliente)
        baseDeDatos = Databases(cliente)
        tablasDB = TablesDB(cliente)
        almacenamiento = Storage(cliente)
        tiempoReal = Realtime(cliente)
    }

    fun init(context: Context) = inicializar(context)

    /*
     * Función que convierte el ID de una foto subida a Appwrite en un enlace web (URL)
     * para poder mostrarla en la pantalla del celular.
     */
    fun obtenerUrlArchivo(idArchivo: String): String {
        // Si ya es un enlace completo (http...), lo devolvemos tal cual
        if (idArchivo.startsWith("http")) return idArchivo
        // Si es un ID de Appwrite Storage, armamos el enlace de visualización
        return "$ENDPOINT/storage/buckets/$ID_BUCKET_FOTOS/files/$idArchivo/view?project=$PROJECT_ID"
    }

    fun getFileViewUrl(fileId: String): String = obtenerUrlArchivo(fileId)
}

typealias AppwriteClient = ClienteAppwrite
