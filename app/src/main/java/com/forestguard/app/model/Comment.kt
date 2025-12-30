package com.forestguard.app.model

data class Comment(
    val id: String,
    val reportId: String,
    val userId: String,
    val userName: String,
    val content: String,
    val avatarId: String?,
    val createdAt: String
)