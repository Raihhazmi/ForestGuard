package com.forestguard.app.data

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage

object AppwriteClient {
    // KITA PAKAI DATA DARI KAMU:
    private const val ENDPOINT = "https://nyc.cloud.appwrite.io/v1"
    private const val PROJECT_ID = "6931838b0036be9509fd"

    lateinit var client: Client
    lateinit var account: Account
    lateinit var databases: Databases
    lateinit var storage: Storage

    fun init(context: Context) {
        client = Client(context)
            .setEndpoint(ENDPOINT)
            .setProject(PROJECT_ID)
            .setSelfSigned(true) // Biarkan true untuk development

        account = Account(client)
        databases = Databases(client)
        storage = Storage(client)
    }
}