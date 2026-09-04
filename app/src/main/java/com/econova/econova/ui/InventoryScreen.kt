package com.econova.econova.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.econova.econova.data.PlantRepository
import com.econova.econova.model.Plant
import com.econova.econova.model.Rarity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(navController: NavController) {
    val plants by PlantRepository.plants.collectAsState()
    val caughtCount = plants.count { it.isCaught }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plant Pokedex (${caughtCount}/${plants.size})", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(padding)
        ) {
            items(plants) { plant ->
                PlantCard(plant) {
                    if (plant.isCaught) {
                        navController.navigate("detail/${plant.id}")
                    }
                }
            }
        }
    }
}

@Composable
fun PlantCard(plant: Plant, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(enabled = plant.isCaught, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (plant.isCaught) Color.White else Color.LightGray.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (plant.isCaught) {
                Icon(
                    Icons.Default.Eco, 
                    contentDescription = null, 
                    modifier = Modifier.size(60.dp),
                    tint = getRarityColor(plant.rarity)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(plant.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(plant.rarity.name, fontSize = 12.sp, color = getRarityColor(plant.rarity))
            } else {
                Icon(
                    Icons.Default.QuestionMark, 
                    contentDescription = null, 
                    modifier = Modifier.size(60.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("???", color = Color.Gray)
            }
        }
    }
}

fun getRarityColor(rarity: Rarity): Color = when (rarity) {
    Rarity.COMMON -> Color(0xFF4CAF50)
    Rarity.UNCOMMON -> Color(0xFF2196F3)
    Rarity.RARE -> Color(0xFF9C27B0)
    Rarity.LEGENDARY -> Color(0xFFFF9800)
}
