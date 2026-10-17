package com.example.app_adopcion_d_mascotas.data.remote

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import io.appwrite.services.TablesDB

object AppwriteClient {
    const val ENDPOINT = "https://fra.cloud.appwrite.io/v1"
    const val PROJECT_ID = "6aceca44001361ba94f0"
    const val DATABASE_ID = "adopcion_db"
    const val BUCKET_ID = "fotos_mascotas"

    const val PETS_COLLECTION = "mascotas"
    const val USERS_COLLECTION = "6acf7f910021e8558b65"
    const val REQUESTS_COLLECTION = "6acf8155000ecaf2f811"
    const val APPOINTMENTS_COLLECTION = "6acf82b100251fdf4d9b"

    private lateinit var client: Client
    lateinit var account: Account
        private set
    lateinit var databases: Databases
        private set
    lateinit var tablesDB: TablesDB
        private set
    lateinit var storage: Storage
        private set

    fun init(context: Context) {
        client = Client(context)
            .setEndpoint(ENDPOINT)
            .setProject(PROJECT_ID)

        account = Account(client)
        databases = Databases(client)
        tablesDB = TablesDB(client)
        storage = Storage(client)
    }

    fun getFileViewUrl(fileId: String): String {
        return if (fileId.startsWith("http")) fileId else "$ENDPOINT/storage/buckets/$BUCKET_ID/files/$fileId/view?project=$PROJECT_ID"
    }
}
