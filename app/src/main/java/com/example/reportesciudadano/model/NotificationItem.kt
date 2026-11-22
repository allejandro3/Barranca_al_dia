package com.example.reportesciudadano.model

data class NotificationItem(
    val id: Long,
    val title: String,
    val message: String,
    val type: String,
    val timestamp: Long
)
