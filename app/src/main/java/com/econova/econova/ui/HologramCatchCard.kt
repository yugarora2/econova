package com.econova.econova.ui

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.econova.econova.model.Plant

private val HologramTint = Color(0xFF4FD8E8)

@Composable
fun HologramCatchCard(
    plant: Plant,
    capturedImage: Bitmap,
    onDismiss: () -> Unit,
    onCatch: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hologram")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .graphicsLayer { translationY = floatOffset }
            ) {
                Image(
                    bitmap = capturedImage.asImageBitmap(),
                    contentDescription = plant.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(HologramTint.copy(alpha = 0.28f))
                )
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    val lineY = size.height * scanProgress
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                HologramTint.copy(alpha = 0.65f),
                                Color.Transparent
                            ),
                            startY = lineY - 40f,
                            endY = lineY + 40f
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(plant.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(plant.scientificName, color = HologramTint, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Badge(containerColor = getRarityColor(plant.rarity)) {
                Text(plant.rarity.name, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Habitat: ${plant.habitat}",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Run Away")
                }
                Button(
                    onClick = onCatch,
                    colors = ButtonDefaults.buttonColors(containerColor = HologramTint)
                ) {
                    Text("Catch", color = Color.Black)
                }
            }
        }
    }
}