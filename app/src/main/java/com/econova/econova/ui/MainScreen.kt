package com.econova.econova.ui

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.core.content.ContextCompat
import com.econova.econova.data.PlantRepository
import androidx.compose.ui.unit.dp
import com.econova.econova.logic.PlantScanner
private val ScanThemeColor = Color(0xFF1B5E20)

private data class LineSpec(
    val x: Float,
    val len: Float,
    val y: Float,
    val thicknessDp: Float
)

@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current

    val cameraProviderFuture =
        remember { ProcessCameraProvider.getInstance(context) }

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var hologramState by remember {
        mutableStateOf<Pair<String, Bitmap>?>(null)
    }
    val plantScanner = remember { PlantScanner(context) }

    Box(modifier = Modifier.fillMaxSize()) {

        // LIVE CAMERA
        AndroidView(
            factory = { ctx ->

                val previewView = PreviewView(ctx).apply {
                    implementationMode =
                        PreviewView.ImplementationMode.COMPATIBLE
                }

                cameraProviderFuture.addListener({

                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(
                                previewView.surfaceProvider
                            )
                        }

                    val cameraSelector =
                        CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()

                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview
                        )

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }, ContextCompat.getMainExecutor(ctx))

                previewViewRef = previewView

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // GREEN OVERLAY + CAMERA CUTOUT
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {

            // ------------------------------------------------
            // Computed once here (in px) so BOTH the tap
            // handler and the draw call can see the same
            // camera-band bounds. These used to be declared
            // *inside* the Canvas draw lambda, which made them
            // invisible to pointerInput's closure above it.
            // ------------------------------------------------

            val screenWidthPx =
                with(density) { maxWidth.toPx() }

            val screenHeightPx =
                with(density) { maxHeight.toPx() }

            val centerXPx = screenWidthPx / 2f

            val cameraTop = screenHeightPx * 0.16f
            val cameraBottom = screenHeightPx * 0.82f

            // Radius of the semicircles
            val radius = screenWidthPx * 0.31f

            // Thickness of the black semicircle
            val archThickness =
                with(density) { 35.dp.toPx() }

            // How far each arc dips into the camera window, and by how
            // much it's rotated. Tuned on the overlay widget as 62px
            // out of a 560px-tall mock, so we keep it as a fraction of
            // the real screen height rather than a fixed px value.
            val arcOverlap = screenHeightPx * (62f / 560f)
            val arcRotationDegrees = 90f

            // Partial-width black lines (tuned on the overlay widget,
            // fractions of screen width/height). Each is
            // (xFraction, lengthFraction, yFraction, thicknessDp).
            val lines = remember {
                listOf(
                    LineSpec(x = 0.00f, len = 0.26f, y = 0.27f, thicknessDp = 8f),
                    LineSpec(x = 0.74f, len = 0.43f, y = 0.27f, thicknessDp = 8f),
                    LineSpec(x = 0.00f, len = 0.26f, y = 0.71f, thicknessDp = 8f),
                    LineSpec(x = 0.74f, len = 0.79f, y = 0.71f, thicknessDp = 8f)
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { tapOffset ->
                                if (tapOffset.y < cameraTop || tapOffset.y > cameraBottom) {
                                    return@detectTapGestures
                                }
                                val previewView = previewViewRef ?: return@detectTapGestures
                                val frame = previewView.bitmap ?: return@detectTapGestures
                                val boxSizePx = (screenWidthPx * 0.90f).toInt()
                                val cropped = cropToCenter(frame, boxSizePx)

                                val detectedId = plantScanner.scanFrame(cropped) ?: return@detectTapGestures
                                val plant = PlantRepository.getPlant(detectedId) ?: return@detectTapGestures

                                PlantRepository.saveCapturedImage(plant.id, cropped)
                                hologramState = plant.id to cropped
                            }
                        )
                    }
            ) {

                // ------------------------------------------------
                // GREEN BACKGROUND
                // ------------------------------------------------

                drawRect(
                    color = ScanThemeColor
                )

                // ------------------------------------------------
                // CLEAR LARGE MIDDLE AREA
                // This is where the LIVE CAMERA is visible.
                // ------------------------------------------------

                drawRect(
                    color = Color.Transparent,
                    topLeft = Offset(
                        0f,
                        cameraTop
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        screenWidthPx,
                        cameraBottom - cameraTop
                    ),
                    blendMode = BlendMode.Clear
                )
                // ------------------------------------------------
                // ------------------------------------------------
// 4 GREEN CORNER MASKS
// These cover the camera outside the radius.
// ------------------------------------------------

                val topBlackLineY = screenHeightPx * 0.27f
                val bottomBlackLineY = screenHeightPx * 0.71f

                val radiusLeft = centerXPx - radius
                val radiusRight = centerXPx + radius

// TOP LEFT
                drawRect(
                    color = ScanThemeColor,
                    topLeft = Offset(
                        0f,
                        cameraTop
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        radiusLeft+33,
                        topBlackLineY - cameraTop
                    )
                )

// TOP RIGHT
                drawRect(
                    color = ScanThemeColor,
                    topLeft = Offset(
                        radiusRight-15,
                        cameraTop
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        screenWidthPx+ - radiusRight+19,
                        topBlackLineY - cameraTop
                    )
                )

// BOTTOM LEFT
                drawRect(
                    color = ScanThemeColor,
                    topLeft = Offset(
                        0f,
                        bottomBlackLineY
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        radiusLeft+33,
                        cameraBottom - bottomBlackLineY
                    )
                )

// BOTTOM RIGHT
                drawRect(
                    color = ScanThemeColor,
                    topLeft = Offset(
                        radiusRight-15,
                        bottomBlackLineY
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        screenWidthPx - radiusRight+19,
                        cameraBottom - bottomBlackLineY
                    )
                )
                // ------------------------------------------------
                // TOP SEMICIRCLE
                // Drawn AFTER the camera-window clear, so it's free
                // to dip (overlap) into the camera band on top of it.
                // Rotated around its own center, which stays on
                // centerXPx so it's always centered on the phone.
                // ------------------------------------------------

                val topArcCenterY = cameraTop + arcOverlap

                rotate(
                    degrees = arcRotationDegrees,
                    pivot = Offset(centerXPx, topArcCenterY)
                ) {
                    drawArc(
                        color = Color.Black,
                        startAngle = 90f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(
                            centerXPx - radius,
                            topArcCenterY - radius
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            radius * 2,
                            radius * 2
                        ),
                        style = Stroke(
                            width = archThickness
                        )
                    )
                }

                // ------------------------------------------------
                // BOTTOM SEMICIRCLE
                // Same idea, dips upward into the camera band.
                // ------------------------------------------------

                val bottomArcCenterY = cameraBottom - arcOverlap

                rotate(
                    degrees = arcRotationDegrees,
                    pivot = Offset(centerXPx, bottomArcCenterY)
                ) {
                    drawArc(
                        color = Color.Black,
                        startAngle = 270f,
                        sweepAngle =180f,
                        useCenter = false,
                        topLeft = Offset(
                            centerXPx - radius,
                            bottomArcCenterY - radius
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            radius * 2,
                            radius * 2
                        ),
                        style = Stroke(
                            width = archThickness
                        )
                    )
                }

                // ------------------------------------------------
                // PARTIAL-WIDTH BLACK LINES
                // Drawn last so they sit on top of everything else.
                // ------------------------------------------------

                lines.forEach { line ->
                    val thicknessPx = with(density) { line.thicknessDp.dp.toPx() }
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(
                            screenWidthPx * line.x,
                            screenHeightPx * line.y - thicknessPx / 2f
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            screenWidthPx * line.len,
                            thicknessPx
                        )
                    )
                }
            }

            // TOP BAR ONLY
            // No Scan button is created here.
            // No bottom-right button is created here.
            Box(
                modifier = Modifier.align(
                    Alignment.TopCenter
                )
            ) {
                EconovaTopBar(
                    onLightBackground = false
                )
            }
        }

        // EXISTING HOLOGRAM/CATCH CARD
        hologramState?.let { (plantId, bitmap) ->

            val plant =
                PlantRepository.getPlant(plantId)

            if (plant != null) {

                HologramCatchCard(
                    plant = plant,
                    capturedImage = bitmap,

                    onDismiss = {
                        hologramState = null
                    },

                    onCatch = {

                        PlantRepository.catchPlant(
                            plantId
                        )

                        hologramState = null

                        navController.navigate(
                            "detail/$plantId"
                        )
                    }
                )
            }
        }
    }
}

private fun cropToCenter(
    source: Bitmap,
    boxSizePx: Int
): Bitmap {

    val cx = source.width / 2
    val cy = source.height / 2

    val half = boxSizePx / 2

    val left =
        (cx - half)
            .coerceIn(0, source.width - 1)

    val top =
        (cy - half)
            .coerceIn(0, source.height - 1)

    val right =
        (cx + half)
            .coerceIn(left + 1, source.width)

    val bottom =
        (cy + half)
            .coerceIn(top + 1, source.height)

    return Bitmap.createBitmap(
        source,
        left,
        top,
        right - left,
        bottom - top
    )
}