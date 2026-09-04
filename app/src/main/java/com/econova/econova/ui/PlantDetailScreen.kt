package com.econova.econova.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.econova.econova.data.PlantRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(plantId: String?, navController: NavController) {
    val plant = plantId?.let { PlantRepository.getPlant(it) } ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plant.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = plant.scientificName,
                fontSize = 18.sp,
                fontStyle = FontStyle.Italic,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Badge(containerColor = getRarityColor(plant.rarity)) {
                Text(plant.rarity.name, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            DetailSection(title = "AI Botanical Guide", content = plant.description)
            DetailSection(title = "Habitat", content = plant.habitat)
            DetailSection(title = "Ecological Importance", content = plant.ecologicalImportance)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            Text(
                "Conservation Awareness", 
                fontWeight = FontWeight.Bold, 
                fontSize = 20.sp, 
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Status: ${plant.conservationStatus}", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(plant.ecologicalImportance) // Reusing for demo
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // AI Chatbot Placeholder
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ask the Botanical Guide", fontWeight = FontWeight.Bold)
                    Text("AI-powered chat coming soon...", fontStyle = FontStyle.Italic, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(content)
    }
}
