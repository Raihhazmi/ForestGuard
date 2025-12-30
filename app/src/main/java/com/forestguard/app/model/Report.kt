package com.forestguard.app.model

data class Report(
    val id: String,
    val userId: String,
    val description: String,
    val severity: Int,
    val imageId: String,
    val createdAt: String,
    val lat: Double,
    val lon: Double,
    val likedBy: List<String> = emptyList(),
    val status: String = "Pending",
    val commentCount: Int = 0 // TAMBAHAN BARU
)