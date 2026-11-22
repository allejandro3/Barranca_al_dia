package com.example.reportesciudadano.model

data class User(
    val name: String,
    val email: String,
    val createdAt: Long = System.currentTimeMillis()
)
