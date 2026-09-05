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
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan"
    )

    // Only a light scrim over the whole screen — the camera feed stays
    // visible behind it, unlike a full opaque takeover.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFF0D1F14))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .graphicsLayer { translationY = floatOffset }
                ) {
                    Image(
                        bitmap = capturedImage.asImageBitmap(),
                        contentDescription = plant.name,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(HologramTint.copy(alpha = 0.28f))
                    )
                    Canvas(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))) {
                        val lineY = size.height * scanProgress
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, HologramTint.copy(alpha = 0.65f), Color.Transparent),
                                startY = lineY - 20f,
                                endY = lineY + 20f
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(plant.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(plant.scientificName, color = HologramTint, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Badge(containerColor = getRarityColor(plant.rarity)) {
                        Text(plant.rarity.name, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "Family", value = plant.family)
            InfoRow(label = "Habitat / Region", value = plant.habitat)
            InfoRow(label = "Ecological Importance", value = plant.ecologicalImportance)
            InfoRow(label = "Conservation Status", value = plant.conservationStatus)

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.align(Alignment.End)) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Dismiss", color = Color.White)
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

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 14.sp)
    }
}