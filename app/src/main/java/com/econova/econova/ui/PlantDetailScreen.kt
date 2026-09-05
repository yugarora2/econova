package com.econova.econova.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material.icons.filled.Delete
import com.econova.econova.data.PlantRepository

@Composable
fun PlantDetailScreen(plantId: String?, navController: NavController) {
    val plantState by PlantRepository.plants.collectAsState()
    val plant = plantId?.let { id -> plantState.find { it.id == id } } ?: return
    
    // If plant is no longer caught (e.g. deleted), go back
    LaunchedEffect(plant.isCaught) {
        if (!plant.isCaught) {
            navController.popBackStack()
        }
    }

    val capturedPaths by PlantRepository.capturedImagePaths.collectAsState()
    val imagePath = capturedPaths[plant.id]
    val capturedBitmap = remember(imagePath) {
        imagePath?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
    }
    val headerColor = getRarityColor(plant.rarity)

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("About", "Conservation")

    Column(modifier = Modifier.fillMaxSize()) {

        // Colored curved header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    color = headerColor,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                "#${plant.id.padStart(3, '0')}",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            )

            var showDeleteConfirm by remember { mutableStateOf(false) }

            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.7f))
            }

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Delete ${plant.name}'s data?") },
                    text = { Text("This removes its captured photo and catch status. You can catch it again later.") },
                    confirmButton = {
                        TextButton(onClick = {
                            PlantRepository.deletePlantData(plant.id)
                            showDeleteConfirm = false
                        }) { Text("Delete", color = Color.Red) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                    }
                )
            }

            Column(
                modifier = Modifier.align(Alignment.Center).padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(plant.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Text(plant.rarity.name, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
            }
        }

        // Floating captured photo overlapping the header curve
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-56).dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .padding(6.dp)
            ) {
                if (capturedBitmap != null) {
                    Image(
                        bitmap = capturedBitmap,
                        contentDescription = plant.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp))
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(headerColor.copy(alpha = 0.2f)))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-40).dp)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                plant.scientificName,
                fontSize = 15.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
                StatusChip(text = plant.habitat, color = headerColor)
                StatusChip(text = plant.conservationStatus, color = Color(0xFF757575))
            }

            Spacer(modifier = Modifier.height(20.dp))

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                when (selectedTab) {
                    0 -> Text(
                        "${plant.description}\n\nHabitat: ${plant.habitat}",
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                    1 -> Text(
                        "Status: ${plant.conservationStatus}\n\nHabitat: ${plant.habitat}\n\n${plant.ecologicalImportance}",
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}