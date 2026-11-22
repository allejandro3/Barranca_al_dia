package com.example.reportesciudadano.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.reportesciudadano.model.Report
import com.example.reportesciudadano.ui.theme.DarkGreen
import com.example.reportesciudadano.ui.theme.LimeGreen
import com.example.reportesciudadano.ui.theme.RedAccent
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CustomMap(
    reports: List<Report>,
    userLocation: Pair<Double, Double>,
    pickedLocation: Pair<Double, Double>?,
    onPickLocation: (Pair<Double, Double>?) -> Unit,
    onReportHereClick: (Pair<Double, Double>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedReport by remember { mutableStateOf<Report?>(null) }
    var currentCenter by remember { mutableStateOf(userLocation) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1B2621))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    selectedReport = null
                    val latOffset = (0.5f - offset.y / size.height) * 0.015
                    val lngOffset = (offset.x / size.width - 0.5f) * 0.015
                    val newLat = currentCenter.first + latOffset
                    val newLng = currentCenter.second + lngOffset
                    onPickLocation(Pair(newLat, newLng))
                }
            }
    ) {
        // Render Satellite & Map Canvas (River, Roads, City Blocks)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Background Terrain
            drawRect(Color(0xFF1B2621))

            // Green Parks & Urban Blocks
            drawCircle(Color(0xFF23352B), radius = width * 0.4f, center = Offset(width * 0.75f, height * 0.35f))
            drawCircle(Color(0xFF23352B), radius = width * 0.3f, center = Offset(width * 0.25f, height * 0.7f))

            // Magdalena River
            val riverPath = Path().apply {
                moveTo(0f, height * 0.18f)
                cubicTo(width * 0.35f, height * 0.12f, width * 0.55f, height * 0.32f, width, height * 0.28f)
            }
            drawPath(riverPath, color = Color(0xFF154D5D), style = Stroke(width = 36f))

            // Main Avenues & Highway
            val avenue1 = Path().apply {
                moveTo(0f, height * 0.48f)
                lineTo(width, height * 0.52f)
            }
            drawPath(avenue1, color = Color(0xFF3B4D45), style = Stroke(width = 20f))

            val avenue2 = Path().apply {
                moveTo(width * 0.42f, 0f)
                lineTo(width * 0.48f, height)
            }
            drawPath(avenue2, color = Color(0xFF3B4D45), style = Stroke(width = 20f))

            // Street Grid Lines
            for (i in 1..7) {
                val y = height * (i / 8f)
                drawLine(Color(0xFF2A3A32), start = Offset(0f, y), end = Offset(width, y), strokeWidth = 5f)
                val x = width * (i / 8f)
                drawLine(Color(0xFF2A3A32), start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 5f)
            }

            // GPS Pulse Blue Circle (User position)
            val userCenter = Offset(width * 0.5f, height * 0.5f)
            drawCircle(Color(0x3D3B82F6), radius = 38f, center = userCenter)
            drawCircle(Color.White, radius = 14f, center = userCenter)
            drawCircle(Color(0xFF3B82F6), radius = 9f, center = userCenter)
        }

        // Render Interactive Report Pins
        reports.forEachIndexed { _, report ->
            val dLat = (report.lat - currentCenter.first) / 0.015
            val dLng = (report.lng - currentCenter.second) / 0.015

            val pinX = 0.5f + dLng.toFloat()
            val pinY = 0.5f - dLat.toFloat()

            if (pinX in 0.05f..0.95f && pinY in 0.05f..0.95f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset {
                            IntOffset(
                                (pinX * 1000).roundToInt(),
                                (pinY * 1800).roundToInt()
                            )
                        }
                        .clickable {
                            selectedReport = report
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = report.t,
                        tint = if (report.mine) Color.White else LimeGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Pick Location Marker
        pickedLocation?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-16).dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Punto elegido",
                    tint = RedAccent,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        // GPS Status Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 16.dp, start = 12.dp)
                .clip(CircleShape)
                .background(DarkGreen.copy(alpha = 0.9f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SatelliteAlt,
                    contentDescription = "GPS",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format(Locale.getDefault(), "GPS activo · %.4f, %.4f", currentCenter.first, currentCenter.second),
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }

        // Re-center Location Button
        IconButton(
            onClick = {
                currentCenter = userLocation
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Mi ubicación",
                tint = DarkGreen,
                modifier = Modifier.size(20.dp)
            )
        }

        // Floating Action Button "Reportar aquí"
        Button(
            onClick = {
                onReportHereClick(pickedLocation ?: currentCenter)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LimeGreen),
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Reportar aquí", color = DarkGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        // Location Selection Bar ("pickBar")
        AnimatedVisibility(
            visible = pickedLocation != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp, start = 12.dp, end = 12.dp)
        ) {
            pickedLocation?.let { loc ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkGreen.copy(alpha = 0.95f))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = RedAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%.5f, %.5f", loc.first, loc.second),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { onPickLocation(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.15f)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Quitar", color = Color.White, fontSize = 11.sp)
                            }

                            Button(
                                onClick = { onReportHereClick(loc) },
                                colors = ButtonDefaults.buttonColors(containerColor = LimeGreen),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Reportar aquí", color = DarkGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Floating Info Card Popup for selected report
        selectedReport?.let { rep ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkGreen)
                    .border(1.dp, LimeGreen, RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .clickable { selectedReport = null }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val imgUrl = rep.photo ?: "https://picsum.photos/seed/${rep.s}/200/160"
                    AsyncImage(
                        model = imgUrl,
                        contentDescription = rep.t,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = rep.t, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "${rep.c} · ${rep.u} · ${if (rep.mine) "Hace un momento" else rep.p}", color = Color.White.copy(0.7f), fontSize = 11.sp)
                    }
                    IconButton(onClick = { selectedReport = null }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
