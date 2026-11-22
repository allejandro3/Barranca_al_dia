package com.example.reportesciudadano.model

data class Report(
    val id: String,
    val t: String,               // Title
    val c: String,               // Category
    val d: String = "",          // Description
    val u: String,               // User name
    val p: String = "",          // Time ago text (e.g. "Hace 10 min")
    val s: String,               // Seed / image type
    val h: Int = 300,            // Height ratio for masonry
    val lat: Double,
    val lng: Double,
    val photo: String? = null,   // Base64 or URI string
    val mine: Boolean = false,
    val st: String = "Pendiente",// Status: "Pendiente", "En proceso", "Atendido", "Rechazado"
    val ts: Long = System.currentTimeMillis(),
    val rs: String = "Riesgo de accidente", // Reason
    val hrs: Int? = null,        // Resolution hours
    val sup: Int = 0,            // Support / Likes count
    val ph: Boolean = false,     // Has photo boolean
    val isSupported: Boolean = false, // User has supported this report
    val dislikes: Int = 0,       // Dislikes count
    val isDisliked: Boolean = false  // User has disliked this report
)

data class ZoneInfo(
    val name: String,
    val lat: Double,
    val lng: Double,
    val weight: Int,
    val factor: Double
)

data class SimReport(
    val ts: Long,
    val c: String,
    val z: String,
    val st: String,
    val rs: String,
    val hrs: Int?,
    val sup: Int,
    val ph: Boolean,
    val t: String,
    val u: String = "Ciudadano",
    val mine: Boolean = false
)
