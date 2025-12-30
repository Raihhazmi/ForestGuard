package com.forestguard.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.forestguard.app.model.Comment
import com.forestguard.app.model.Forest
import com.forestguard.app.model.Report
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.models.InputFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ReportRepository {
    private val storage = AppwriteClient.storage
    private val databases = AppwriteClient.databases
    private val account = AppwriteClient.account

    // --- ID DATABASE & COLLECTION ---
    private val DATABASE_ID = "693187c3003c1b09baf1"
    private val COLLECTION_ID = "reports" // Reports
    private val COMMENT_COLLECTION_ID = "6938f8f8002bfea05f2e" // Comments
    private val FOREST_COLLECTION_ID = "forests" // Forests
    private val BUCKET_ID = "693185d3000a10335bd8"
    private val PROJECT_ID = "6931838b0036be9509fd"
    private val ENDPOINT = "https://nyc.cloud.appwrite.io/v1"

    suspend fun getCurrentUserId(): String {
        return try { account.get().id } catch (e: Exception) { "" }
    }

    // 1. GET REPORTS (Termasuk commentCount)
    suspend fun getReports(): List<Report> {
        return withContext(Dispatchers.IO) {
            try {
                val response = databases.listDocuments(
                    databaseId = DATABASE_ID,
                    collectionId = COLLECTION_ID,
                    queries = listOf(Query.orderDesc("\$createdAt"))
                )
                response.documents.map { doc ->
                    Report(
                        id = doc.id,
                        userId = doc.data["userId"] as? String ?: "Anonim",
                        description = doc.data["description"] as? String ?: "-",
                        severity = (doc.data["severity"] as? Number)?.toInt() ?: 1,
                        imageId = doc.data["imageId"] as? String ?: "",
                        createdAt = doc.createdAt,
                        lat = (doc.data["latitude"] as? Number)?.toDouble() ?: 0.0,
                        lon = (doc.data["longitude"] as? Number)?.toDouble() ?: 0.0,
                        likedBy = (doc.data["likedBy"] as? List<Any>)?.map { it.toString() } ?: emptyList(),
                        status = doc.data["status"] as? String ?: "Pending",
                        // AMBIL JUMLAH KOMENTAR
                        commentCount = (doc.data["commentCount"] as? Number)?.toInt() ?: 0
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    // 2. ADD COMMENT & UPDATE COUNT
    suspend fun addComment(reportId: String, content: String) {
        withContext(Dispatchers.IO) {
            try {
                val user = account.get()
                val prefs = try { account.getPrefs().data } catch (e: Exception) { emptyMap<String, Any>() }
                val avatar = prefs["avatarId"] as? String ?: ""
                val userName = if (user.name.isNotEmpty()) user.name else "Relawan ${user.id.take(4)}"

                // 1. Simpan Komentar
                val commentData = mapOf(
                    "reportId" to reportId,
                    "userId" to user.id,
                    "userName" to userName,
                    "content" to content,
                    "avatarId" to avatar
                )
                databases.createDocument(DATABASE_ID, COMMENT_COLLECTION_ID, ID.unique(), commentData)

                // 2. Update Jumlah Komentar di Report (+1)
                // Ambil data report dulu untuk tahu jumlah sekarang
                val reportDoc = databases.getDocument(DATABASE_ID, COLLECTION_ID, reportId)
                val currentCount = (reportDoc.data["commentCount"] as? Number)?.toInt() ?: 0

                // Update ke database
                databases.updateDocument(
                    DATABASE_ID,
                    COLLECTION_ID,
                    reportId,
                    data = mapOf("commentCount" to currentCount + 1)
                )

            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // ... (Fungsi Lain Tidak Berubah, tetap dicopy biar lengkap) ...

    suspend fun getComments(reportId: String): List<Comment> {
        return withContext(Dispatchers.IO) {
            try {
                val response = databases.listDocuments(
                    databaseId = DATABASE_ID,
                    collectionId = COMMENT_COLLECTION_ID,
                    queries = listOf(Query.equal("reportId", reportId), Query.orderDesc("\$createdAt"))
                )
                response.documents.map { doc ->
                    Comment(
                        id = doc.id,
                        reportId = doc.data["reportId"] as String,
                        userId = doc.data["userId"] as String,
                        userName = doc.data["userName"] as String,
                        content = doc.data["content"] as String,
                        avatarId = doc.data["avatarId"] as? String,
                        createdAt = doc.createdAt
                    )
                }
            } catch (e: Exception) { emptyList() }
        }
    }

    suspend fun uploadReport(context: Context, imageUri: Uri, description: String, severity: Int, latitude: Double, longitude: Double, onSuccess: () -> Unit, onError: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val file = compressImage(context, imageUri)
                val inputFile = InputFile.fromFile(file)
                val uploadedFile = storage.createFile(bucketId = BUCKET_ID, fileId = ID.unique(), file = inputFile)
                val user = account.get()

                val data = mapOf(
                    "userId" to user.id,
                    "description" to description,
                    "severity" to severity,
                    "imageId" to uploadedFile.id,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "status" to "Pending",
                    "likedBy" to emptyList<String>(),
                    "commentCount" to 0 // Default 0 saat buat report baru
                )
                databases.createDocument(DATABASE_ID, COLLECTION_ID, ID.unique(), data)
                withContext(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onError(e.message ?: "Gagal Upload") }
            }
        }
    }

    suspend fun toggleLike(reportId: String, currentLikes: List<String>) {
        withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                if (userId.isEmpty()) return@withContext
                val newLikes = currentLikes.toMutableList()
                if (newLikes.contains(userId)) newLikes.remove(userId) else newLikes.add(userId)
                databases.updateDocument(DATABASE_ID, COLLECTION_ID, reportId, data = mapOf("likedBy" to newLikes))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    suspend fun updateReportStatus(reportId: String, status: String) {
        withContext(Dispatchers.IO) {
            try { databases.updateDocument(DATABASE_ID, COLLECTION_ID, reportId, data = mapOf("status" to status)) } catch (e: Exception) { e.printStackTrace() }
        }
    }

    suspend fun deleteReport(reportId: String) {
        withContext(Dispatchers.IO) {
            try { databases.deleteDocument(DATABASE_ID, COLLECTION_ID, reportId) } catch (e: Exception) { e.printStackTrace() }
        }
    }

    suspend fun getForests(): List<Forest> {
        return withContext(Dispatchers.IO) {
            try {
                val response = databases.listDocuments(DATABASE_ID, FOREST_COLLECTION_ID)
                response.documents.map { doc ->
                    Forest(
                        id = doc.id,
                        name = doc.data["name"] as? String ?: "Hutan",
                        region = doc.data["region"] as? String ?: "-",
                        lat = (doc.data["latitude"] as? Number)?.toDouble() ?: 0.0,
                        lon = (doc.data["longitude"] as? Number)?.toDouble() ?: 0.0,
                        status = doc.data["status"] as? String ?: "Aman",
                        isAlert = doc.data["isAlert"] as? Boolean ?: false
                    )
                }
            } catch (e: Exception) { e.printStackTrace(); emptyList() }
        }
    }

    private fun compressImage(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val file = File(context.cacheDir, "temp.jpg")
        val outputStream = FileOutputStream(file)
        BitmapFactory.decodeStream(inputStream).compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        return file
    }

    fun getImageUrl(imageId: String): String {
        return "$ENDPOINT/storage/buckets/$BUCKET_ID/files/$imageId/view?project=$PROJECT_ID"
    }
}