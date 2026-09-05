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
                id = "1", name = "Areca Palm", scientificName = "Areca catechu", family = "Arecaceae",
                description = "A slender palm tree widely cultivated for its seed, the areca nut.",
                habitat = "Tropical lowlands", ecologicalImportance = "Common ornamental and cultivated palm.",
                conservationStatus = "Least Concern", rarity = Rarity.COMMON
            ),
            Plant(
                id = "2", name = "Gooseberry Plant", scientificName = "Phyllanthus emblica", family = "Phyllanthaceae",
                description = "A deciduous tree known for its edible, vitamin C-rich fruit.",
                habitat = "Tropical & Subtropical", ecologicalImportance = "Widely cultivated fruit and medicinal plant.",
                conservationStatus = "Least Concern", rarity = Rarity.COMMON
            ),
            Plant(
                id = "3", name = "Jasmine", scientificName = "Jasminum", family = "Oleaceae",
                description = "A fragrant flowering shrub or vine, popular for its scent.",
                habitat = "Gardens & warm climates", ecologicalImportance = "Attracts pollinators; culturally significant.",
                conservationStatus = "Least Concern", rarity = Rarity.COMMON
            ),
            Plant(
                id = "4", name = "Neerium", scientificName = "Nerium oleander", family = "Apocynaceae",
                description = "An evergreen shrub with clusters of showy flowers.",
                habitat = "Indian subcontinent", ecologicalImportance = "Hardy ornamental, drought tolerant.",
                conservationStatus = "Least Concern", rarity = Rarity.NATIVE
            ),
            Plant(
                id = "5", name = "Red Pong Pong", scientificName = "Cerbera manghas", family = "Apocynaceae",
                description = "A flowering tree with red-eyed white blossoms and toxic fruit.",
                habitat = "Coastal regions", ecologicalImportance = "Provides habitat structure near coastlines.",
                conservationStatus = "Least Concern", rarity = Rarity.COMMON
            ),
            Plant(
                id = "6", name = "Spiny Brinjal", scientificName = "Solanum virginianum", family = "Solanaceae",
                description = "A thorny herb used traditionally for its medicinal roots and fruit.",
                habitat = "Indian subcontinent", ecologicalImportance = "Traditional medicinal plant.",
                conservationStatus = "Least Concern", rarity = Rarity.NATIVE
            ),
            Plant(
                id = "7", name = "Veldt Grape", scientificName = "Cissus quadrangularis", family = "Vitaceae",
                description = "A succulent climbing vine with distinctive four-angled stems.",
                habitat = "Indian subcontinent", ecologicalImportance = "Used in traditional medicine; hardy climber.",
                conservationStatus = "Least Concern", rarity = Rarity.NATIVE
            ),
            Plant(
                id = "8", name = "Velvet Beans", scientificName = "Mucuna pruriens", family = "Fabaceae",
                description = "A climbing legume covered in fine, irritant hairs on its pods.",
                habitat = "Tropical regions", ecologicalImportance = "Nitrogen-fixing cover crop.",
                conservationStatus = "Least Concern", rarity = Rarity.NATIVE
            ),
            Plant(
                id = "9", name = "Wild Plumeria", scientificName = "Plumeria", family = "Apocynaceae",
                description = "A flowering tree known for its fragrant, waxy blossoms.",
                habitat = "Tropical gardens", ecologicalImportance = "Popular ornamental; attracts moths.",
                conservationStatus = "Least Concern", rarity = Rarity.COMMON
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
    fun deletePlantData(id: String) {
        val currentList = _plants.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isCaught = false)
            _plants.value = currentList
        }

        _capturedImages.value = _capturedImages.value - id
        _capturedImagePaths.value = _capturedImagePaths.value - id

        if (isInitialized) {
            repoScope.launch {
                PlantImageStore.deleteImage(appContext, id)
                PlantDataStore.setUncaught(appContext, id)
            }
        }
    }
    fun getPlant(id: String): Plant? = _plants.value.find { it.id == id }
}