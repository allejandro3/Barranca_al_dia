package com.example.reportesciudadano.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.reportesciudadano.model.Comment
import com.example.reportesciudadano.model.Report
import com.example.reportesciudadano.ui.theme.DarkGreen
import com.example.reportesciudadano.ui.theme.LimeGreen
import com.example.reportesciudadano.ui.theme.RedAccent

@Composable
fun ReportCardInsta(
    report: Report,
    onSupportClick: (String) -> Unit,
    onDislikeClick: (String) -> Unit,
    onClick: () -> Unit,
    loadComments: (String) -> List<Comment>,
    onAddComment: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isLiked by remember(report.isSupported) { mutableStateOf(report.isSupported) }
    var isDisliked by remember(report.isDisliked) { mutableStateOf(report.isDisliked) }
    var showCommentsDialog by remember { mutableStateOf(false) }
    var commentsList by remember(report.id) { mutableStateOf<List<Comment>>(emptyList()) }
    var newCommentText by remember { mutableStateOf("") }

    LaunchedEffect(showCommentsDialog, report.id) {
        if (showCommentsDialog) {
            commentsList = loadComments(report.id)
        }
    }

    val heartScale by animateFloatAsState(
        targetValue = if (isLiked) 1.3f else 1.0f,
        animationSpec = spring(),
        label = "heart_scale"
    )
    val heartColor by animateColorAsState(
        targetValue = if (isLiked) RedAccent else Color.White,
        label = "heart_color"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkGreen)
            .clickable(onClick = onClick)
            .padding(bottom = 4.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(LimeGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (report.u.isNotEmpty()) report.u.take(1).uppercase() else "U",
                    color = DarkGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = report.u,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${report.c} · ${if (report.mine) "Hace un momento" else report.p}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        // Image
        val imageUrl = report.photo ?: "https://picsum.photos/seed/${report.s}/600/420"
        val isAttended = report.st.equals("Atendido", true) || report.st.equals("Verificado", true)
        val badgeColor = if (isAttended) LimeGreen else Color(0xFFFBBF24)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = report.t,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // Status Badge Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkGreen.copy(alpha = 0.9f))
                    .border(1.dp, badgeColor, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Estado",
                        tint = badgeColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = report.st,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Actions & Title Only (Like, Dislike, Comment)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                // Like Button
                IconButton(
                    onClick = {
                        isLiked = !isLiked
                        if (isLiked && isDisliked) isDisliked = false
                        onSupportClick(report.id)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Me gusta",
                        tint = heartColor,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(heartScale)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                // Dislike Button
                IconButton(
                    onClick = {
                        isDisliked = !isDisliked
                        if (isDisliked && isLiked) isLiked = false
                        onDislikeClick(report.id)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isDisliked) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                        contentDescription = "No me gusta",
                        tint = if (isDisliked) RedAccent else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Comment Button
                IconButton(
                    onClick = { showCommentsDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comentar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Text(
                text = report.t,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Ubicación",
                    tint = LimeGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = report.c,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }

    // Comment Dialog
    if (showCommentsDialog) {
        AlertDialog(
            onDismissRequest = { showCommentsDialog = false },
            containerColor = DarkGreen,
            title = {
                Text(text = "Comentarios (${commentsList.size})", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    if (commentsList.isEmpty()) {
                        Text(text = "Sé el primero en comentar este reporte.", color = Color.White.copy(0.7f), fontSize = 13.sp)
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 12.dp)
                        ) {
                            commentsList.forEach { comment ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = comment.userName,
                                        color = LimeGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = comment.text, color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Escribe un comentario...", color = Color.White.copy(0.6f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LimeGreen,
                            unfocusedBorderColor = Color.White.copy(0.5f)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val text = newCommentText.trim()
                        if (text.isNotEmpty()) {
                            onAddComment(report.id, text)
                            commentsList = loadComments(report.id)
                            newCommentText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LimeGreen)
                ) {
                    Text("Comentar", color = DarkGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCommentsDialog = false }) {
                    Text("Cerrar", color = Color.White)
                }
            }
        )
    }
}
