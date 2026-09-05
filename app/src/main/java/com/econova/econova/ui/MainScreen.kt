package com.econova.econova.ui

import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.econova.econova.data.PlantRepository
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat

private const val RETICLE_SIZE_DP = 250

@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var hologramState by remember { mutableStateOf<Pair<String, Bitmap>?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    // COMPATIBLE mode is required so previewView.bitmap can capture frames
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
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
                previewViewRef = previewView
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        AROverlay(
            onReticleTap = {
                val previewView = previewViewRef ?: return@AROverlay
                val frame = previewView.bitmap ?: return@AROverlay

                // TODO: once the TFLite model is wired in, replace the two lines
                // below with the model's actual detected plant ID and its real
                // bounding box, instead of a random pick + reticle-center crop.
                val uncaught = PlantRepository.plants.value.filter { !it.isCaught }
                if (uncaught.isEmpty()) return@AROverlay
                val plant = uncaught.random()

                val boxSizePx = with(density) { RETICLE_SIZE_DP.dp.toPx() }.toInt()
                val cropped = cropToReticle(frame, boxSizePx)

                PlantRepository.saveCapturedImage(plant.id, cropped)
                hologramState = plant.id to cropped
            }
        )

        hologramState?.let { (plantId, bitmap) ->
            val plant = PlantRepository.getPlant(plantId)
            if (plant != null) {
                HologramCatchCard(
                    plant = plant,
                    capturedImage = bitmap,
                    onDismiss = { hologramState = null },
                    onCatch = {
                        PlantRepository.catchPlant(plantId)
                        hologramState = null
                        navController.navigate("detail/$plantId")
                    }
                )
            }
        }
    }
}

private fun cropToReticle(source: Bitmap, boxSizePx: Int): Bitmap {
    val cx = source.width / 2
    val cy = source.height / 2
    val half = boxSizePx / 2
    val left = (cx - half).coerceIn(0, source.width - 1)
    val top = (cy - half).coerceIn(0, source.height - 1)
    val right = (cx + half).coerceIn(left + 1, source.width)
    val bottom = (cy + half).coerceIn(top + 1, source.height)
    return Bitmap.createBitmap(source, left, top, right - left, bottom - top)
}

@Composable
fun AROverlay(onReticleTap: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(RETICLE_SIZE_DP.dp)
                .align(Alignment.Center)
                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onReticleTap() })
                }
        )
    }
}