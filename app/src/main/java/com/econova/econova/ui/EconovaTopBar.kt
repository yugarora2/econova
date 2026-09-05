package com.econova.econova.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EconovaTopBar(onLightBackground: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (onLightBackground) Color.White else Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Eco,
            contentDescription = null,
            tint = if (onLightBackground) Color(0xFF2E7D32) else Color.White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Econova",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = if (onLightBackground) Color.Black else Color.White
        )
    }
}