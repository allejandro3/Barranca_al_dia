package com.example.reportesciudadano.ui.components

import android.annotation.SuppressLint
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.reportesciudadano.model.Report
import com.example.reportesciudadano.ui.theme.DarkGreen
import com.example.reportesciudadano.ui.theme.LimeGreen
import com.example.reportesciudadano.ui.theme.RedAccent
import java.util.Locale

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewMap(
    reports: List<Report>,
    userLocation: Pair<Double, Double>,
    pickedLocation: Pair<Double, Double>?,
    onPickLocation: (Pair<Double, Double>?) -> Unit,
    onReportHereClick: (Pair<Double, Double>) -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val reportsJson = remember(reports) {
        "[" + reports.joinToString(separator = ",") { r ->
            val safeTitle = r.t.replace("\\", "\\\\").replace("'", "\\'")
            val safeCat = r.c.replace("\\", "\\\\").replace("'", "\\'")
            val safeUser = r.u.replace("\\", "\\\\").replace("'", "\\'")
            "{\"lat\": ${r.lat}, \"lng\": ${r.lng}, \"title\": \"$safeTitle\", \"category\": \"$safeCat\", \"user\": \"$safeUser\"}"
        } + "]"
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    settings.loadsImagesAutomatically = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.evaluateJavascript("if(typeof addReportMarkers === 'function') { addReportMarkers('$reportsJson'); map.invalidateSize(); }", null)
                        }
                    }

                    addJavascriptInterface(
                        WebAppInterface { lat, lng ->
                            onPickLocation(Pair(lat, lng))
                        },
                        "AndroidBridge"
                    )
                    loadUrl("file:///android_asset/map.html")
                    webViewInstance = this
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { wv ->
                if (pickedLocation == null) {
                    wv.evaluateJavascript(
                        "if(typeof pickMarker !== 'undefined' && pickMarker) { map.removeLayer(pickMarker); pickMarker = null; }",
                        null
                    )
                }
            }
        )

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

class WebAppInterface(private val onPick: (Double, Double) -> Unit) {
    @JavascriptInterface
    fun onMapClicked(lat: Double, lng: Double) {
        onPick(lat, lng)
    }
}
