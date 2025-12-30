package com.forestguard.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.InputFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AuthRepository {
    private val account = AppwriteClient.account
    private val storage = AppwriteClient.storage

    // ID Project & Bucket Appwrite
    private val PROJECT_ID = "6931838b0036be9509fd"
    private val BUCKET_ID = "693185d3000a10335bd8" // Bucket Avatar
    private val ENDPOINT = "https://nyc.cloud.appwrite.io/v1"

    suspend fun getUserData(): Pair<String, String?> {
        return try {
            val user = account.get()
            val name = if (user.name.isNotEmpty()) user.name else "Ranger"

            // Ambil Avatar dari Prefs
            val prefs = try { account.getPrefs().data } catch (e: Exception) { emptyMap<String, Any>() }
            val avatarId = prefs["avatarId"] as? String
            val avatarUrl = if (avatarId != null) getImageUrl(avatarId) else null

            Pair(name, avatarUrl)
        } catch (e: Exception) {
            Pair("Tamu", null)
        }
    }

    suspend fun getUser(): String? {
        return try { account.get().email } catch (e: Exception) { null }
    }

    suspend fun login(email: String, pass: String): Result<Boolean> {
        return try {
            try { account.deleteSession("current") } catch (_: Exception) {}
            account.createEmailPasswordSession(email, pass)
            Result.success(true)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }

    // --- PERBAIKAN DI SINI: Menambahkan parameter 'name' ---
    suspend fun register(email: String, pass: String, name: String = ""): Result<Boolean> {
        return try {
            // 1. Buat Akun (Kirim Nama juga)
            account.create(
                userId = ID.unique(),
                email = email,
                password = pass,
                name = name // <-- Nama disimpan di sini
            )

            // 2. Langsung Login otomatis setelah daftar
            login(email, pass)

            Result.success(true)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try { account.deleteSession("current") } catch (_: Exception) {}
    }

    suspend fun uploadProfilePicture(context: Context, uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val file = compressImage(context, uri)
                val inputFile = InputFile.fromFile(file)

                val uploadedFile = storage.createFile(
                    bucketId = BUCKET_ID,
                    fileId = ID.unique(),
                    file = inputFile
                )

                // Simpan ID Foto ke Preferences User
                val currentPrefs = try { account.getPrefs().data } catch (e: Exception) { emptyMap<String, Any>() }
                val newPrefs = currentPrefs.toMutableMap()
                newPrefs["avatarId"] = uploadedFile.id

                account.updatePrefs(newPrefs)

                Result.success(getImageUrl(uploadedFile.id))
            } catch (e: Exception) {
                Result.failure(Exception("Gagal Upload: ${e.message}"))
            }
        }
    }

    private fun compressImage(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val width = 500
        val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
        val height = (width / ratio).toInt()
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)

        val file = File(context.cacheDir, "avatar.jpg")
        val outputStream = FileOutputStream(file)
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        outputStream.flush()
        outputStream.close()
        return file
    }

    private fun getImageUrl(imageId: String): String {
        return "$ENDPOINT/storage/buckets/$BUCKET_ID/files/$imageId/view?project=$PROJECT_ID"
    }
}