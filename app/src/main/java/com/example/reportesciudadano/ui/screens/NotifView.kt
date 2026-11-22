package com.example.reportesciudadano.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reportesciudadano.data.ReportsRepository
import com.example.reportesciudadano.model.NotificationItem
import com.example.reportesciudadano.model.Report
import com.example.reportesciudadano.model.SimulatedData
import com.example.reportesciudadano.ui.theme.DarkGreen
import com.example.reportesciudadano.ui.theme.LimeGreen

@Composable
fun NotifView(
    repository: ReportsRepository,
    onNotificationClick: (Report) -> Unit,
    modifier: Modifier = Modifier
) {
    val allNotifications = repository.getNotifications()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGreen)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Notificaciones y Reportes",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Avisos, apoyos e historial de tus reportes creados",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (allNotifications.isEmpty()) {
            Text(
                text = "No tienes notificaciones recientes.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        } else {
            allNotifications.forEach { notif ->
                val icon = when (notif.type) {
                    "ATTENDED" -> Icons.Default.Build
                    "SUPPORT" -> Icons.Default.ThumbUp
                    else -> Icons.Default.CheckCircle
                }
                NotifItem(
                    icon = icon,
                    message = notif.message,
                    onClick = {
                        val matchingReport = repository.reports.firstOrNull() ?: SimulatedData.INITIAL_REPORTS.first()
                        onNotificationClick(matchingReport)
                    }
                )
            }
        }
    }
}

@Composable
private fun NotifItem(
    icon: ImageVector,
    message: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LimeGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}
