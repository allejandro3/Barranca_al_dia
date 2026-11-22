package com.example.reportesciudadano.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reportesciudadano.ui.theme.DarkGreen
import com.example.reportesciudadano.ui.theme.LimeGreen
import kotlinx.coroutines.delay

@Composable
fun NavDrawer(
    isOpen: Boolean,
    onClose: () -> Unit,
    onNavigate: (AppView) -> Unit,
    currentView: AppView? = null
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim with fade in/out
        AnimatedVisibility(
            visible = isOpen,
            enter = fadeIn(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose
                    )
            )
        }

        // Drawer panel with slide + fade
        AnimatedVisibility(
            visible = isOpen,
            enter = slideInHorizontally(
                animationSpec = tween(320, easing = FastOutSlowInEasing),
                initialOffsetX = { -it }
            ) + fadeIn(animationSpec = tween(200)),
            exit = slideOutHorizontally(
                animationSpec = tween(240, easing = FastOutSlowInEasing),
                targetOffsetX = { -it }
            ) + fadeOut(animationSpec = tween(180)),
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.76f)
                .align(Alignment.CenterStart)
        ) {
            DrawerContent(
                isOpen = isOpen,
                currentView = currentView,
                onClose = onClose,
                onNavigate = onNavigate
            )
        }
    }
}

@Composable
private fun DrawerContent(
    isOpen: Boolean,
    currentView: AppView?,
    onClose: () -> Unit,
    onNavigate: (AppView) -> Unit
) {
    var itemsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isOpen) {
        if (isOpen) {
            delay(90)
            itemsVisible = true
        } else {
            itemsVisible = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreen)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Vertical + WindowInsetsSides.Start
                )
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Barranca al Día",
                    color = LimeGreen,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Menú",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar menú",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val entries = listOf(
            DrawerEntry(Icons.Default.Map, "Mapa de reportes", AppView.MAP),
            DrawerEntry(Icons.Default.Home, "Publicaciones", AppView.HOME),
            DrawerEntry(Icons.Default.Notifications, "Notificaciones", AppView.NOTIF),
            DrawerEntry(Icons.Default.Person, "Perfil", AppView.USER)
        )

        entries.forEachIndexed { index, entry ->
            DrawerItem(
                icon = entry.icon,
                text = entry.text,
                isSelected = currentView == entry.view,
                index = index,
                visible = itemsVisible,
                onClick = {
                    onClose()
                    onNavigate(entry.view)
                }
            )
        }
    }
}

private data class DrawerEntry(
    val icon: ImageVector,
    val text: String,
    val view: AppView
)

@Composable
private fun DrawerItem(
    icon: ImageVector,
    text: String,
    isSelected: Boolean,
    index: Int,
    visible: Boolean,
    onClick: () -> Unit
) {
    val delayMillis = if (visible) index * 40 else 0

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 260, delayMillis = delayMillis),
        label = "drawer_item_alpha"
    )
    val translation by animateFloatAsState(
        targetValue = if (visible) 0f else -28f,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = "drawer_item_translation"
    )
    val itemBackground by animateColorAsState(
        targetValue = if (isSelected) LimeGreen.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "drawer_item_background"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) LimeGreen else Color.White,
        animationSpec = tween(durationMillis = 200),
        label = "drawer_item_icon"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) LimeGreen else Color.White,
        animationSpec = tween(durationMillis = 200),
        label = "drawer_item_text"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    this.alpha = alpha
                    translationX = translation.dp.toPx()
                }
                .clip(RoundedCornerShape(10.dp))
                .background(itemBackground)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
        HorizontalDivider(
            color = Color.White.copy(alpha = 0.08f),
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
