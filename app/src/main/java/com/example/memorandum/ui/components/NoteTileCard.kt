package com.example.memorandum.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memorandum.R
import com.example.memorandum.domain.model.Note
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Star
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.scale
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.outlined.Star
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteTileCard(
    note: Note,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit, // 1. Am adăugat parametrul pentru click pe stea
    onLongClick: () -> Unit = {},
    isSelected: Boolean = false
) {
    val cardColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            note.isFavorite -> Color(0xFFFFF8E1)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = 300),
        label = "cardColor"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (note.isFavorite && !isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "glowAlpha"
    )

    val textColor = if (note.isFavorite && !isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = if (note.isFavorite && !isSelected) Color.Black.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .then(
                if (note.isFavorite && !isSelected)
                    Modifier.border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = glowAlpha),
                                Color(0xFFFFA000).copy(alpha = glowAlpha)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (note.isFavorite && !isSelected) 6.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title.ifBlank { stringResource(R.string.no_title) },
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = textColor
                )

                // 2. Modificat: Acum steaua se afișează MEREU (plină sau goală) și reacționează la click
                val starColor by animateColorAsState(
                    targetValue = if (note.isFavorite) Color(0xFFFFD700) else secondaryTextColor,
                    animationSpec = tween(durationMillis = 300),
                    label = "starColor"
                )
                val starScale by animateFloatAsState(
                    targetValue = if (note.isFavorite) 1.2f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "starScale"
                )

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(28.dp) // Dimensiune puțin mai mică pentru a se încadra bine în tile
                ) {
                    Icon(
                        imageVector = if (note.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = stringResource(R.string.favorite),
                        tint = starColor,
                        modifier = Modifier
                            .size(20.dp)
                            .scale(starScale)
                    )
                }
            }
            if (note.content.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = note.content.trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = secondaryTextColor
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = remember(note.createdAt) {
                    android.text.format.DateFormat.format("dd.MM.yyyy • HH:mm", note.createdAt).toString()
                },
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryTextColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}