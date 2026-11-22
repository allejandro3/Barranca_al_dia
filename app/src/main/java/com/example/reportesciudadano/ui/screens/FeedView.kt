package com.example.reportesciudadano.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reportesciudadano.model.Comment
import com.example.reportesciudadano.model.Report
import com.example.reportesciudadano.ui.components.FeedMode
import com.example.reportesciudadano.ui.components.ReportCardInsta
import com.example.reportesciudadano.ui.components.ReportCardPin
import com.example.reportesciudadano.ui.theme.DarkGreen
import com.example.reportesciudadano.ui.theme.LimeGreen
import com.example.reportesciudadano.ui.theme.MidGreen

@Composable
fun FeedView(
    reports: List<Report>,
    searchQuery: String,
    mode: FeedMode,
    onSupportClick: (String) -> Unit,
    onDislikeClick: (String) -> Unit,
    loadComments: (String) -> List<Comment>,
    onAddComment: (String, String) -> Unit,
    onReportClick: (Report) -> Unit,
    onOpenFormClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredReports = remember(reports, searchQuery) {
        if (searchQuery.isBlank()) {
            reports
        } else {
            val q = searchQuery.lowercase()
            reports.filter { (it.t + it.c + it.d + it.u).lowercase().contains(q) }
        }
    }

    val instaListState = rememberLazyListState()
    val pinListState = rememberLazyListState()
    val activeListState = if (mode == FeedMode.INSTA) instaListState else pinListState

    val isFabVisible by remember(activeListState) {
        derivedStateOf {
            val firstVisibleIndex = activeListState.firstVisibleItemIndex
            val firstVisibleOffset = activeListState.firstVisibleItemScrollOffset
            firstVisibleIndex == 0 || firstVisibleOffset < 50
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidGreen)
    ) {
        if (filteredReports.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sin resultados.",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        } else {
            AnimatedContent(
                targetState = mode,
                transitionSpec = {
                    val direction = if (targetState == FeedMode.PIN) 1 else -1
                    (slideInHorizontally(
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) { full -> full / 5 * direction } +
                            fadeIn(tween(220)) +
                            scaleIn(initialScale = 0.97f, animationSpec = tween(300))) togetherWith
                            (slideOutHorizontally(
                                animationSpec = tween(220, easing = FastOutSlowInEasing)
                            ) { full -> -full / 5 * direction } +
                                    fadeOut(tween(160)))
                },
                label = "feed_mode_transition"
            ) { targetMode ->
                if (targetMode == FeedMode.INSTA) {
                    LazyColumn(
                        state = instaListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredReports, key = { it.id }) { report ->
                            ReportCardInsta(
                                report = report,
                                onSupportClick = onSupportClick,
                                onDislikeClick = onDislikeClick,
                                onClick = { onReportClick(report) },
                                loadComments = loadComments,
                                onAddComment = onAddComment,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                } else {
                    // Pinterest 2-column layout simulation
                    val leftList = filteredReports.filterIndexed { idx, _ -> idx % 2 == 0 }
                    val rightList = filteredReports.filterIndexed { idx, _ -> idx % 2 != 0 }

                    LazyColumn(
                        state = pinListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    leftList.forEach { report ->
                                        ReportCardPin(
                                            report = report,
                                            onClick = { onReportClick(report) }
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rightList.forEach { report ->
                                        ReportCardPin(
                                            report = report,
                                            onClick = { onReportClick(report) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Scroll-sensitive Animated FAB
        AnimatedVisibility(
            visible = isFabVisible,
            enter = slideInVertically(tween(260)) { it } +
                    fadeIn(tween(200)) +
                    scaleIn(initialScale = 0.8f, animationSpec = tween(260)),
            exit = slideOutVertically(tween(180)) { it } +
                    fadeOut(tween(140)) +
                    scaleOut(targetScale = 0.8f, animationSpec = tween(180)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
        ) {
            FloatingActionButton(
                onClick = onOpenFormClick,
                containerColor = LimeGreen,
                contentColor = DarkGreen,
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Reportar aquí",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reportar aquí",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
