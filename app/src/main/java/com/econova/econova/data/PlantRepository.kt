package com.econova.econova.data

import android.content.Context
import com.econova.econova.model.Plant
import com.econova.econova.model.Rarity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import android.graphics.Bitmap

object PlantRepository {
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var appContext: Context
    private var isInitialized = false

    private val _plants = MutableStateFlow(
        listOf(
            Plant(
                "1", "Candi-leaf", "Stevia rebaudiana",
                "A sweet leaf plant known for its natural sweetness.",
                "Tropical/Subtropical", "Natural sweetener source.", "Vulnerable", Rarity.COMMON
            ),
            Plant(
                "2", "Neem", "Azadirachta indica",
                "A tree in the mahogany family with medicinal properties.",
                "Drier regions", "Natural pesticide and medicine.", "Least Concern", Rarity.COMMON
            ),
            Plant(
                "3", "Holy Basil", "Ocimum tenuiflorum",
                "An aromatic perennial plant known as Tulsi.",
                "Indian subcontinent", "Cultural and medicinal significance.", "Least Concern", Rarity.NATIVE
            ),
            Plant(
                "4", "Golden Shower", "Cassia fistula",
                "A flowering plant with bright yellow pendulous flowers.",
                "Tropical forests", "Attracts bees and butterflies.", "Least Concern", Rarity.COMMON
            ),
            Plant(
                "5", "Banyan", "Ficus benghalensis",
                "A large tree that starts its life as an epiphyte.",
                "Indian subcontinent", "A miniature ecosystem in itself.", "Least Concern", Rarity.NATIVE
            ),
            Plant(
                "6", "Peepal", "Ficus religiosa",
                "A species of fig native to the Indian subcontinent.",
                "Indo-Gangetic Plain", "Significant oxygen producer.", "Least Concern", Rarity.NATIVE
            ),
            Plant(
                "7", "Amla", "Phyllanthus emblica",
                "The Indian gooseberry, famous for its Vitamin C.",
                "Tropical & Subtropical", "Highly medicinal and edible.", "Data Deficient", Rarity.NATIVE
            )
        )
    )
    private val _capturedImages = MutableStateFlow<Map<String, Bitmap>>(emptyMap())
    val capturedImages = _capturedImages.asStateFlow()

    // File path per plant ID, once the write to disk completes. UI screens
// observe this (rather than the in-memory bitmap map) so photos are
// available again after an app restart.
    private val _capturedImagePaths = MutableStateFlow<Map<String, String>>(emptyMap())
    val capturedImagePaths = _capturedImagePaths.asStateFlow()

    fun saveCapturedImage(id: String, bitmap: Bitmap) {
        // Show it immediately in the hologram card this session.
        _capturedImages.value = _capturedImages.value + (id to bitmap)

        if (isInitialized) {
            repoScope.launch {
                val path = PlantImageStore.saveImage(appContext, id, bitmap)
                _capturedImagePaths.value = _capturedImagePaths.value + (id to path)
            }
        }
    }

    fun getCapturedImage(id: String): Bitmap? = _capturedImages.value[id]

    val plants = _plants.asStateFlow()
    val caughtPlants = plants.map { it.filter { p -> p.isCaught } }

    /**
     * Call once, e.g. from Application.onCreate(), before the UI reads `plants`.
     * Loads previously caught plant IDs from disk and applies them.
     */
    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        appContext = context.applicationContext

        _capturedImagePaths.value = PlantImageStore.existingImagePaths(appContext)

        repoScope.launch {
            val caughtIds = PlantDataStore.caughtIdsFlow(appContext).first()
            if (caughtIds.isNotEmpty()) {
                val updated = _plants.value.map { plant ->
                    if (plant.id in caughtIds) plant.copy(isCaught = true) else plant
                }
                _plants.value = updated
            }
        }
    }
    fun catchPlant(id: String) {
        val currentList = _plants.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isCaught = true)
            _plants.value = currentList

            if (isInitialized) {
                repoScope.launch {
                    PlantDataStore.setCaught(appContext, id)
                }
            }
        }
    }

    fun getPlant(id: String): Plant? = _plants.value.find { it.id == id }
}