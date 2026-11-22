package com.example.reportesciudadano.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.example.reportesciudadano.model.Report
import com.example.reportesciudadano.ui.theme.DarkGreen
import com.example.reportesciudadano.ui.theme.LimeGreen
import com.example.reportesciudadano.ui.theme.RedAccent
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import java.util.Locale
import kotlin.math.abs

@Composable
fun MapLibreMapView(
    reports: List<Report>,
    userLocation: Pair<Double, Double>,
    pickedLocation: Pair<Double, Double>?,
    onPickLocation: (Pair<Double, Double>?) -> Unit,
    onReportHereClick: (Pair<Double, Double>) -> Unit,
    onReportClick: (Report) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    remember(context) {
        MapLibre.getInstance(context)
    }

    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }
    var mapLibreMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var selectedReportOnMap by remember { mutableStateOf<Report?>(null) }

    val barrancabermeja = remember(userLocation) { LatLng(userLocation.first, userLocation.second) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val mapView = mapViewInstance ?: return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewInstance?.onDestroy()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapLibre.getInstance(ctx)
                MapView(ctx).apply {
                    mapViewInstance = this
                    getMapAsync { map ->
                        mapLibreMapInstance = map
                        map.setStyle("https://tiles.openfreemap.org/styles/liberty") {
                            map.cameraPosition = CameraPosition.Builder()
                                .target(barrancabermeja)
                                .zoom(14.0)
                                .build()

                            // Handle map single click
                            map.addOnMapClickListener { latLng ->
                                selectedReportOnMap = null
                                onPickLocation(Pair(latLng.latitude, latLng.longitude))
                                true
                            }

                            // Handle marker clicks
                            map.setOnMarkerClickListener { marker ->
                                val clicked = reports.find { r ->
                                    abs(r.lat - marker.position.latitude) < 0.0005 &&
                                            abs(r.lng - marker.position.longitude) < 0.0005
                                }
                                if (clicked != null) {
                                    selectedReportOnMap = clicked
                                }
                                true
                            }
                        }
                    }
                }
            },
            update = { _ ->
                val map = mapLibreMapInstance ?: return@AndroidView
                map.clear()

                // Add markers for all reports
                reports.forEach { r ->
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(r.lat, r.lng))
                            .title(r.t)
                            .snippet("${r.c} · ${r.u}")
                    )
                }

                // Add marker for picked location if any
                pickedLocation?.let { loc ->
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(loc.first, loc.second))
                            .title("Reportar aquí")
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Animated Floating Info Card Popup for selected marker on map
        AnimatedVisibility(
            visible = selectedReportOnMap != null,
            enter = slideInVertically(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                initialOffsetY = { -it }
            ) + fadeIn(tween(220)) + scaleIn(initialScale = 0.9f, animationSpec = spring(0.4f, 400f)),
            exit = slideOutVertically(
                animationSpec = tween(220, easing = FastOutSlowInEasing),
                targetOffsetY = { -it }
            ) + fadeOut(tween(160)) + scaleOut(targetScale = 0.9f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            selectedReportOnMap?.let { rep ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkGreen)
                        .border(1.dp, LimeGreen, RoundedCornerShape(12.dp))
                        .clickable { onReportClick(rep) }
                        .padding(12.dp)
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
                            Text(text = "${rep.c} · ${rep.u}", color = Color.White.copy(0.7f), fontSize = 11.sp)
                            Text(text = "Toca para ver detalles ➔", color = LimeGreen, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        }
                        IconButton(onClick = { selectedReportOnMap = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Top-left GPS Status Badge
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
                    text = String.format(Locale.getDefault(), "GPS activo · %.4f, %.4f", userLocation.first, userLocation.second),
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }

        // Top-right Re-center Button
        IconButton(
            onClick = {
                mapLibreMapInstance?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(barrancabermeja, 14.0)
                )
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

        // Bottom-right "Reportar aquí" Floating Button
        Button(
            onClick = {
                onReportHereClick(pickedLocation ?: userLocation)
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

        // Bottom Pick Location Bar ("pickBar")
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
    }
}
