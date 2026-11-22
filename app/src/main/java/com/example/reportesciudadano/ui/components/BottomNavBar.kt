package com.example.reportesciudadano.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.reportesciudadano.ui.theme.DarkGreen
import com.example.reportesciudadano.ui.theme.LimeGreen
import com.example.reportesciudadano.ui.theme.MidGreen

enum class AppView {
    MAP, HOME, NOTIF, USER
}

enum class FeedMode {
    INSTA, PIN
}

@Composable
fun BottomNavBar(
    currentView: AppView,
    feedMode: FeedMode,
    onMenuClick: () -> Unit,
    onViewSelect: (AppView) -> Unit,
    onToggleFeedMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkGreen, MidGreen)
                )
            )
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                )
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Menu Button
        NavButton(
            isSelected = false,
            onClick = onMenuClick
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menú",
                tint = DarkGreen,
                modifier = Modifier.size(18.dp)
            )
        }

        // 2. Search / Map Button
        NavButton(
            isSelected = currentView == AppView.MAP,
            onClick = { onViewSelect(AppView.MAP) }
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Mapa",
                tint = DarkGreen,
                modifier = Modifier.size(18.dp)
            )
        }

        // 3. Home / Feed Button (Center - Larger)
        NavButton(
            isSelected = currentView == AppView.HOME,
            isCenter = true,
            onClick = {
                if (currentView != AppView.HOME) {
                    onViewSelect(AppView.HOME)
                } else {
                    onToggleFeedMode()
                }
            }
        ) {
            AnimatedContent(
                targetState = feedMode,
                transitionSpec = {
                    (scaleIn(initialScale = 0.5f, animationSpec = tween(180)) + fadeIn(tween(160))) togetherWith
                            (scaleOut(targetScale = 0.5f, animationSpec = tween(140)) + fadeOut(tween(120)))
                },
                label = "feed_mode_icon"
            ) { mode ->
                Icon(
                    imageVector = if (mode == FeedMode.PIN) Icons.Default.GridView else Icons.Default.Home,
                    contentDescription = if (mode == FeedMode.PIN) "Vista Pinterest" else "Vista Publicaciones",
                    tint = DarkGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 4. Notifications / Bell Button
        NavButton(
            isSelected = currentView == AppView.NOTIF,
            onClick = { onViewSelect(AppView.NOTIF) }
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notificaciones",
                tint = DarkGreen,
                modifier = Modifier.size(18.dp)
            )
        }

        // 5. User / Profile Button
        NavButton(
            isSelected = currentView == AppView.USER,
            onClick = { onViewSelect(AppView.USER) }
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Perfil",
                tint = DarkGreen,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun NavButton(
    isSelected: Boolean,
    isCenter: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 700f),
        label = "nav_scale"
    )

    val borderAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "nav_border"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) LimeGreen else LimeGreen.copy(alpha = 0.92f),
        animationSpec = tween(durationMillis = 200),
        label = "nav_background"
    )

    val width = if (isCenter) 52.dp else 40.dp
    val height = if (isCenter) 36.dp else 30.dp

    Box(
        modifier = Modifier
            .scale(scale)
            .size(width = width, height = height)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(2.dp, Color.White.copy(alpha = borderAlpha), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
