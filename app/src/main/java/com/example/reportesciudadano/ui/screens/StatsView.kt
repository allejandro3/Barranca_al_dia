package com.example.reportesciudadano.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reportesciudadano.model.Report
import com.example.reportesciudadano.model.SimReport
import com.example.reportesciudadano.model.SimulatedData
import com.example.reportesciudadano.ui.components.AreaChart
import com.example.reportesciudadano.ui.components.DonutChart
import com.example.reportesciudadano.ui.components.HorizontalBarChart
import com.example.reportesciudadano.ui.components.PolarAreaChart
import com.example.reportesciudadano.ui.components.VerticalBarChart
import com.example.reportesciudadano.ui.components.WeeklyHeatmap
import com.example.reportesciudadano.ui.theme.ChartColors
import com.example.reportesciudadano.ui.theme.DarkGreen
import com.example.reportesciudadano.ui.theme.LimeGreen
import com.example.reportesciudadano.ui.theme.StatusColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatsView(
    userReports: List<Report>,
    simulatedReports: List<SimReport>,
    modifier: Modifier = Modifier
) {
    var rangeDays by remember { mutableIntStateOf(30) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var selectedDay by remember { mutableStateOf<String?>(null) }
    var selectedBand by remember { mutableStateOf<String?>(null) }
    var selectedComunas by remember { mutableStateOf<Set<String>>(emptySet()) }
    var onlyMine by remember { mutableStateOf(false) }

    val allReports = remember(userReports, simulatedReports) {
        val userSim = userReports.map { r ->
            SimReport(
                ts = r.ts,
                c = r.c,
                z = SimulatedData.getZoneNameFor(r.lat, r.lng),
                st = r.st,
                rs = r.rs,
                hrs = r.hrs,
                sup = r.sup,
                ph = r.photo != null,
                t = r.t,
                u = r.u,
                mine = true
            )
        }
        userSim + simulatedReports
    }

    val filtered = remember(allReports, rangeDays, selectedCategory, selectedStatus, selectedReason, selectedDay, selectedBand, selectedComunas, onlyMine) {
        val minTs = System.currentTimeMillis() - rangeDays * 86400000L
        allReports.filter { r ->
            val cal = Calendar.getInstance().apply { timeInMillis = r.ts }
            val dayOfWeekIdx = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
            val dayName = SimulatedData.DAYS.getOrElse(dayOfWeekIdx) { "" }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val bandName = when (hour) {
                in 0..3 -> "0–3 h"
                in 4..7 -> "4–7 h"
                in 8..11 -> "8–11 h"
                in 12..15 -> "12–15 h"
                in 16..19 -> "16–19 h"
                else -> "20–23 h"
            }

            r.ts >= minTs &&
                    (selectedCategory == null || r.c == selectedCategory) &&
                    (selectedStatus == null || r.st == selectedStatus) &&
                    (selectedReason == null || r.rs == selectedReason) &&
                    (selectedDay == null || dayName == selectedDay) &&
                    (selectedBand == null || bandName == selectedBand) &&
                    (selectedComunas.isEmpty() || selectedComunas.contains(r.z)) &&
                    (!onlyMine || r.mine)
        }
    }

    val totalCount = filtered.size
    val attendedCount = filtered.count { it.st == "Atendido" }
    val attendedPct = if (totalCount > 0) (attendedCount * 100) / totalCount else 0
    val totalSupports = filtered.sumOf { r -> r.sup }
    val timeNow = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }

    // Dynamic Last 7 Days calculation based on filtered reports
    val sevenDaysAgo = System.currentTimeMillis() - 7 * 86400000L
    val last7DaysCount = remember(filtered) {
        filtered.count { it.ts >= sevenDaysAgo }
    }

    // Dynamic Average Resolution Time calculation based on filtered reports
    val resolvedReports = remember(filtered) { filtered.filter { it.hrs != null } }
    val avgResolutionHours = remember(resolvedReports) {
        if (resolvedReports.isNotEmpty()) resolvedReports.map { it.hrs!! }.average() else 62.4
    }
    val resolutionText = remember(avgResolutionHours) {
        if (avgResolutionHours < 48) {
            "${avgResolutionHours.toInt()} h"
        } else {
            String.format(Locale.getDefault(), "%.1f días", avgResolutionHours / 24.0).replace('.', ',')
        }
    }

    // Dynamic Peak Hour calculation based on filtered reports
    val hourCounts = remember(filtered) {
        (0..23).map { h ->
            filtered.count {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.ts
                cal.get(Calendar.HOUR_OF_DAY) == h
            }
        }
    }
    val peakHourIdx = hourCounts.indices.maxByOrNull { hourCounts[it] } ?: 17
    val peakHourStr = String.format(Locale.getDefault(), "%02d:00", peakHourIdx)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGreen)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header & Update Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PieChart, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Estadísticas", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text("Lo que reporta la ciudad, en un vistazo.", color = Color.White.copy(0.75f), fontSize = 12.sp)
            }

            Button(
                onClick = { /* Refresh */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.15f)),
                shape = CircleShape,
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Actualizar", color = Color.White, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter status pills row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (onlyMine) LimeGreen else Color.White.copy(0.15f))
                    .clickable { onlyMine = !onlyMine }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Solo mis reportes (${userReports.size})",
                    color = if (onlyMine) DarkGreen else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(text = "$totalCount en este periodo", color = Color.White, fontSize = 11.sp)
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(text = "${filtered.size} visibles", color = Color.White, fontSize = 11.sp)
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(text = "Actualizado $timeNow", color = Color.White, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Time Range Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(7, 30, 90).forEach { days ->
                val isSelected = rangeDays == days
                Button(
                    onClick = { rangeDays = days },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) LimeGreen else Color.White.copy(0.15f)
                    ),
                    shape = CircleShape
                ) {
                    Text(
                        text = "$days días",
                        color = if (isSelected) DarkGreen else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5 Dropdown Filters Row (Tipo, Estado, Razón, Día, Franja)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterDropdown("Tipo", selectedCategory, SimulatedData.CATEGORIES) { selectedCategory = it }
            FilterDropdown("Estado", selectedStatus, SimulatedData.STATUSES) { selectedStatus = it }
            FilterDropdown("Razón", selectedReason, SimulatedData.REASONS) { selectedReason = it }
            FilterDropdown("Día", selectedDay, SimulatedData.DAYS) { selectedDay = it }
            FilterDropdown("Franja", selectedBand, SimulatedData.BANDS) { selectedBand = it }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Comuna Multi-select Pills Filter Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comunas (Toca varias para comparar)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // "Todas" pill
                val isAllSelected = selectedComunas.isEmpty()
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isAllSelected) LimeGreen else Color.White.copy(0.15f))
                        .clickable { selectedComunas = emptySet() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Todas",
                        color = if (isAllSelected) DarkGreen else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                SimulatedData.ZONES.forEach { zone ->
                    val isSelected = selectedComunas.contains(zone.name)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) LimeGreen else Color.White.copy(0.15f))
                            .clickable {
                                selectedComunas = if (isSelected) {
                                    selectedComunas - zone.name
                                } else {
                                    selectedComunas + zone.name
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = zone.name,
                            color = if (isSelected) DarkGreen else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (selectedCategory != null || selectedStatus != null || selectedReason != null || selectedDay != null || selectedBand != null || selectedComunas.isNotEmpty() || onlyMine) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filtros activos aplicados · ${filtered.size} de $totalCount reportes",
                        color = Color.White.copy(0.7f),
                        fontSize = 11.sp
                    )
                    TextButton(
                        onClick = {
                            selectedCategory = null
                            selectedStatus = null
                            selectedReason = null
                            selectedDay = null
                            selectedBand = null
                            selectedComunas = emptySet()
                            onlyMine = false
                        }
                    ) {
                        Text("Limpiar filtros", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // KPI Cards Grid (6 cards synchronized)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 2
        ) {
            KpiCard(
                icon = Icons.Default.Flag,
                value = totalCount.toString(),
                label = "Reportes en el periodo",
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                icon = Icons.Default.CheckCircle,
                value = "$attendedPct%",
                label = "Atendidos",
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                icon = Icons.Default.Schedule,
                value = resolutionText,
                label = "Resolución media",
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                icon = Icons.Default.DateRange,
                value = last7DaysCount.toString(),
                label = "Últimos 7 días",
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                icon = Icons.Default.Schedule,
                value = peakHourStr,
                label = "Hora pico",
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                icon = Icons.Default.ThumbUp,
                value = totalSupports.toString(),
                label = "Apoyos ciudadanos",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Donut Chart Card: Tipos de reporte (Interactive)
        val catCounts = SimulatedData.CATEGORIES.map { cat -> filtered.count { it.c == cat } }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(16.dp)
        ) {
            Column {
                Text("Tipos de reporte", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Toca una sección para filtrar.", color = Color.White.copy(0.7f), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DonutChart(
                        values = catCounts.map { it.toFloat() },
                        colors = ChartColors,
                        labels = SimulatedData.CATEGORIES,
                        onSliceClick = { idx ->
                            selectedCategory = if (idx < 0) null else SimulatedData.CATEGORIES.getOrNull(idx)
                        },
                        modifier = Modifier.size(130.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SimulatedData.CATEGORIES.forEachIndexed { i, cat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    selectedCategory = if (selectedCategory == cat) null else cat
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(ChartColors.getOrElse(i) { Color.Gray })
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$cat (${catCounts.getOrElse(i) { 0 }})",
                                    color = if (selectedCategory == cat) LimeGreen else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Polar Area Chart: Razón de reporte (Interactive)
        val reasonCounts = SimulatedData.REASONS.map { r -> filtered.count { it.rs == r } }
        val maxReasonCount = reasonCounts.maxOrNull() ?: 1
        val maxReasonIdx = reasonCounts.indexOf(maxReasonCount)
        val topReasonName = SimulatedData.REASONS.getOrElse(maxReasonIdx) { "Riesgo de accidente" }
        val topReasonPct = if (totalCount > 0) (maxReasonCount * 100) / totalCount else 0
        val reasonSubtitle = if (totalCount > 0) "«$topReasonName» es la razón más frecuente ($topReasonPct%)." else "Sin reportes en este periodo."

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Razón de reporte", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(reasonSubtitle, color = Color.White.copy(0.7f), fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))

                PolarAreaChart(
                    values = reasonCounts.map { it.toFloat() },
                    labels = SimulatedData.REASONS,
                    colors = ChartColors,
                    onSectorClick = { idx ->
                        selectedReason = if (idx < 0) null else SimulatedData.REASONS.getOrNull(idx)
                    },
                    modifier = Modifier.size(190.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    maxItemsInEachRow = 3
                ) {
                    SimulatedData.REASONS.forEachIndexed { i, reason ->
                        val isSelected = selectedReason == reason
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) Color.White.copy(0.2f) else Color.Transparent)
                                .clickable {
                                    selectedReason = if (selectedReason == reason) null else reason
                                }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ChartColors.getOrElse(i) { Color.Gray })
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = reason,
                                color = if (isSelected) LimeGreen else Color.White,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Horizontal Bar Chart: Comunas con más reportes (Interactive)
        val zoneNames = SimulatedData.ZONES.map { it.name }
        val zoneCounts = zoneNames.map { z -> filtered.count { it.z == z } }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(16.dp)
        ) {
            Column {
                Text("Comunas con más reportes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = if (selectedComunas.isEmpty()) "Mostrando todas las comunas." else "Comparando: ${selectedComunas.joinToString(", ")}",
                    color = Color.White.copy(0.7f),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                HorizontalBarChart(
                    labels = zoneNames,
                    values = zoneCounts,
                    onBarClick = { idx ->
                        if (idx < 0) {
                            selectedComunas = emptySet()
                        } else {
                            val zone = SimulatedData.ZONES.getOrNull(idx)?.name
                            if (zone != null) {
                                selectedComunas = if (selectedComunas.contains(zone)) {
                                    selectedComunas - zone
                                } else {
                                    selectedComunas + zone
                                }
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Area Chart: Horas en que más se reporta (Interactive)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(16.dp)
        ) {
            Column {
                Text("Horas en que más se reporta", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Pico a las $peakHourStr. Toca un punto para filtrar por franja horaria.", color = Color.White.copy(0.7f), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))

                AreaChart(
                    values = hourCounts,
                    onPointClick = { hourIdx ->
                        if (hourIdx < 0) {
                            selectedBand = null
                        } else {
                            selectedBand = when (hourIdx) {
                                in 0..3 -> "0–3 h"
                                in 4..7 -> "4–7 h"
                                in 8..11 -> "8–11 h"
                                in 12..15 -> "12–15 h"
                                in 16..19 -> "16–19 h"
                                else -> "20–23 h"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Weekly Heatmap (Interactive)
        val heatmapMatrix = SimulatedData.DAYS.mapIndexed { dIdx, _ ->
            SimulatedData.BANDS.mapIndexed { bIdx, _ ->
                filtered.count { r ->
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = r.ts
                    val day = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                    val band = cal.get(Calendar.HOUR_OF_DAY) / 4
                    day == dIdx && band == bIdx
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(16.dp)
        ) {
            Column {
                Text("Mapa de calor semanal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Toca una celda para filtrar por día y franja.", color = Color.White.copy(0.7f), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))

                WeeklyHeatmap(
                    matrix = heatmapMatrix,
                    days = SimulatedData.DAYS,
                    bands = SimulatedData.BANDS,
                    onCellClick = { dayIdx, bandIdx ->
                        if (dayIdx < 0 || bandIdx < 0) {
                            selectedDay = null
                            selectedBand = null
                        } else {
                            selectedDay = SimulatedData.DAYS.getOrNull(dayIdx)
                            selectedBand = SimulatedData.BANDS.getOrNull(bandIdx)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Vertical Bar Chart: Reportes por día (Interactive)
        val dayCounts = SimulatedData.DAYS.mapIndexed { dIdx, _ ->
            filtered.count { r ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = r.ts
                val day = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                day == dIdx
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(16.dp)
        ) {
            Column {
                Text("Reportes por día de la semana", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Toca una barra para filtrar por día.", color = Color.White.copy(0.7f), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))

                VerticalBarChart(
                    labels = SimulatedData.DAYS.map { it.take(3) },
                    values = dayCounts,
                    onBarClick = { dayIdx ->
                        selectedDay = if (dayIdx < 0) null else SimulatedData.DAYS.getOrNull(dayIdx)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Top Supported Ranking Card (Interactive)
        val topSupported = filtered.sortedByDescending { it.sup }.take(5)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(16.dp)
        ) {
            Column {
                Text("Reportes más apoyados", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Los que más vecinos respaldan.", color = Color.White.copy(0.7f), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))

                topSupported.forEachIndexed { idx, rep ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                selectedCategory = rep.c
                                selectedComunas = setOf(rep.z)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(LimeGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (idx + 1).toString(),
                                color = DarkGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rep.t,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${rep.z} · ${rep.c}",
                                color = Color.White.copy(0.7f),
                                fontSize = 11.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ThumbUp, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = rep.sup.toString(),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (idx < topSupported.size - 1) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterDropdown(
    label: String,
    selectedValue: String?,
    options: List<String>,
    onSelect: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkGreen)
            .border(1.dp, Color.White.copy(0.4f), RoundedCornerShape(6.dp))
            .clickable { expanded = true }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.width(95.dp)
        ) {
            Text(
                text = selectedValue ?: label,
                color = if (selectedValue != null) LimeGreen else Color.White,
                fontSize = 11.sp,
                maxLines = 1
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(label) },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun KpiCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(12.dp)
    ) {
        Column {
            Icon(imageVector = icon, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(18.dp))
            Text(text = value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            Text(text = label, color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
        }
    }
}
