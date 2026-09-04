package com.econova.econova.ui

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.econova.econova.data.PlantRepository
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch

@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    var showCatchDialog by remember { mutableStateOf(false) }
    var detectedPlantId by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview (AR Live Feed)
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Gamified HUD / AR Overlay Layer
        AROverlay(onScanClick = {
            // Simulate detection of a random uncaught plant for demo
            val uncaught = PlantRepository.plants.value.filter { !it.isCaught }
            if (uncaught.isNotEmpty()) {
                val plant = uncaught.random()
                detectedPlantId = plant.id
                showCatchDialog = true
            }
        })

        if (showCatchDialog && detectedPlantId != null) {
            CatchDialog(
                plantId = detectedPlantId!!,
                onDismiss = { showCatchDialog = false },
                onCatch = {
                    PlantRepository.catchPlant(detectedPlantId!!)
                    showCatchDialog = false
                    navController.navigate("detail/${detectedPlantId}")
                }
            )
        }
    }
}

@Composable
fun AROverlay(onScanClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Reticle / Scanner View
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.Center)
                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
        )

        // Scan Button (Pokeball style)
        Button(
            onClick = onScanClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .size(80.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                Icons.Default.Search, 
                contentDescription = "Scan", 
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun CatchDialog(plantId: String, onDismiss: () -> Unit, onCatch: () -> Unit) {
    val plant = PlantRepository.getPlant(plantId) ?: return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wild ${plant.name} Appeared!") },
        text = { Text("It looks like a ${plant.rarity} species found in ${plant.habitat}. Do you want to scan and save it to your Pokedex?") },
        confirmButton = {
            Button(onClick = onCatch) {
                Text("Scan & Catch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Run Away")
            }
        }
    )
}
