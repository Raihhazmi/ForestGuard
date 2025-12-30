package com.forestguard.app.model

data class Forest(
    val id: String,
    val name: String,
    val region: String,
    val lat: Double,
    val lon: Double,
    val status: String,
    val isAlert: Boolean
)