package com.example.reportesciudadano.model

data class Comment(
    val id: Long,
    val reportId: String,
    val userName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
