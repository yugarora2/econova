package com.econova.econova.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.econova.econova.data.PlantRepository
import com.econova.econova.model.Plant
import com.econova.econova.model.Rarity

@Composable
fun InventoryScreen(navController: NavController) {
    val plants by PlantRepository.plants.collectAsState()
    val capturedPaths by PlantRepository.capturedImagePaths.collectAsState()
    var query by remember { mutableStateOf("") }

    val filtered = remember(plants, query) {
        if (query.isBlank()) plants
        else plants.filter { it.name.contains(query, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        EconovaTopBar()

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered) { plant ->
                PokedexStyleCard(
                    plant = plant,
                    imagePath = capturedPaths[plant.id],
                    onClick = {
                        if (plant.isCaught) navController.navigate("detail/${plant.id}")
                    }
                )
            }
        }
    }
}

@Composable
fun PokedexStyleCard(plant: Plant, imagePath: String?, onClick: () -> Unit) {
    val capturedBitmap = remember(imagePath) {
        imagePath?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
    }
    val bgColor = if (plant.isCaught) getRarityColor(plant.rarity).copy(alpha = 0.18f)
    else Color(0xFFEDEDED)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .clickable(enabled = plant.isCaught, onClick = onClick)
            .padding(10.dp)
    ) {
        Text(
            "#${plant.id.padStart(3, '0')}",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.TopEnd)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (plant.isCaught) {
                if (capturedBitmap != null) {
                    Image(
                        bitmap = capturedBitmap,
                        contentDescription = plant.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Icon(
                        Icons.Default.Eco,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = getRarityColor(plant.rarity)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(plant.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            } else {
                Icon(
                    Icons.Default.QuestionMark,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("???", color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}

fun getRarityColor(rarity: Rarity): Color = when (rarity) {
    Rarity.COMMON -> Color(0xFF4CAF50)
    Rarity.NATIVE -> Color(0xFF9C27B0)
}