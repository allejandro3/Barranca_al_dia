package com.example.reportesciudadano.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.reportesciudadano.model.Report
import com.example.reportesciudadano.ui.theme.DarkGreen
import com.example.reportesciudadano.ui.theme.LimeGreen
import com.example.reportesciudadano.ui.theme.RedAccent
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun ReportDetailScreen(
    report: Report,
    onBack: () -> Unit,
    onSupportClick: (String) -> Unit,
    onDislikeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var hasSupported by remember(report.id) { mutableStateOf(report.isSupported) }
    var currentSupports by remember(report.id, report.sup) { mutableStateOf(report.sup) }
    var isPulsing by remember { mutableStateOf(false) }

    var hasDisliked by remember(report.id) { mutableStateOf(report.isDisliked) }
    var currentDislikes by remember(report.id, report.dislikes) { mutableStateOf(report.dislikes) }
    var isDislikePulsing by remember { mutableStateOf(false) }

    val buttonScale by animateFloatAsState(
        targetValue = if (isPulsing) 1.12f else 1.0f,
        animationSpec = spring(dampingRatio = 0.35f, stiffness = 500f),
        label = "support_button_scale"
    )

    val dislikeButtonScale by animateFloatAsState(
        targetValue = if (isDislikePulsing) 1.12f else 1.0f,
        animationSpec = spring(dampingRatio = 0.35f, stiffness = 500f),
        label = "dislike_button_scale"
    )

    LaunchedEffect(isPulsing) {
        if (isPulsing) {
            delay(180)
            isPulsing = false
        }
    }

    LaunchedEffect(isDislikePulsing) {
        if (isDislikePulsing) {
            delay(180)
            isDislikePulsing = false
        }
    }

    val isAttended = report.st.equals("Atendido", true) || report.st.equals("Verificado", true)
    val badgeText = report.st
    val badgeColor = if (isAttended) LimeGreen else Color(0xFFFBBF24)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGreen)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar with Back Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            Text(
                text = "Detalle del Reporte",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Report Image Header
        val imgUrl = report.photo ?: "https://picsum.photos/seed/${report.s}/800/500"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            AsyncImage(
                model = imgUrl,
                contentDescription = report.t,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Status Badge Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkGreen.copy(alpha = 0.9f))
                    .border(1.dp, badgeColor, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Estado",
                        tint = badgeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Details Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = report.t,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(LimeGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = report.c,
                        color = LimeGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(0.7f), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = report.u,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.White.copy(0.7f), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = report.p,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            // Description Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Descripción detallada",
                        color = LimeGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = report.d.ifEmpty { "Sin descripción detallada provista por el ciudadano." },
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Extra details not visible at first glance (Apoyos comunitarios removed)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Información técnica y verificación",
                        color = LimeGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    DetailRow(label = "Estado de revisión", value = report.st)
                    DetailRow(label = "Razón / Causa", value = report.rs)
                    DetailRow(label = "Tiempo estimado de solución", value = if (report.hrs != null) "${report.hrs} horas" else "24-48 horas")
                    DetailRow(label = "Ubicación GPS", value = String.format(Locale.getDefault(), "Lat: %.4f, Lng: %.4f", report.lat, report.lng))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Like & Dislike Buttons Side by Side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Like Button
                Button(
                    onClick = {
                        isPulsing = true
                        hasSupported = !hasSupported
                        currentSupports = if (hasSupported) currentSupports + 1 else (currentSupports - 1).coerceAtLeast(0)
                        if (hasSupported && hasDisliked) {
                            hasDisliked = false
                            currentDislikes = (currentDislikes - 1).coerceAtLeast(0)
                        }
                        onSupportClick(report.id)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .scale(buttonScale),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasSupported) Color(0xFF374151) else LimeGreen
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        imageVector = if (hasSupported) Icons.Default.Favorite else Icons.Default.ThumbUp,
                        contentDescription = null,
                        tint = if (hasSupported) RedAccent else DarkGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasSupported) "Apoyado ($currentSupports)" else "Apoyar ($currentSupports)",
                        color = if (hasSupported) Color.White else DarkGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Dislike Button
                Button(
                    onClick = {
                        isDislikePulsing = true
                        hasDisliked = !hasDisliked
                        currentDislikes = if (hasDisliked) currentDislikes + 1 else (currentDislikes - 1).coerceAtLeast(0)
                        if (hasDisliked && hasSupported) {
                            hasSupported = false
                            currentSupports = (currentSupports - 1).coerceAtLeast(0)
                        }
                        onDislikeClick(report.id)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .scale(dislikeButtonScale),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasDisliked) RedAccent else Color.White.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbDown,
                        contentDescription = null,
                        tint = if (hasDisliked) Color.White else Color.White.copy(0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasDisliked) "Rechazado ($currentDislikes)" else "Rechazar ($currentDislikes)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White.copy(0.7f), fontSize = 13.sp)
        Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
