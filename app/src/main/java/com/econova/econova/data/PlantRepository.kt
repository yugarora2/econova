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
                habitat = "Tropical lowlands",
                ecologicalImportance = "Why it matters: Shades homes, purifies air, and its leaves double up as eco-friendly plates and packaging.\n\n" +
                        "Threats: Overharvesting for betel-nut trade and monoculture farming reduce genetic diversity.\n\n" +
                        "Best practices: Grow in mixed gardens, not just plantations; diversity keeps it resilient.\n\n" +
                        "How to help: Choose areca leaf plates over small plastic swaps; real impact.",
                conservationStatus = "Least Concern", rarity = Rarity.COMMON
            ),
            Plant(
                id = "2", name = "Gooseberry Plant", scientificName = "Phyllanthus emblica", family = "Phyllanthaceae",
                description = "A deciduous tree known for its edible, vitamin C-rich fruit.",
                habitat = "Tropical & Subtropical",
                ecologicalImportance = "Why it matters: A vitamin-C powerhouse for people and a food source for birds and insects.\n\n" +
                        "Threats: Wild trees are stripped bare for Ayurvedic demand faster than they regrow.\n\n" +
                        "Best practices: Harvest only ripe fruit, and leave enough for birds and the next season.\n\n" +
                        "How to help: Buy farmed amla and plant a sapling in your backyard.",
                conservationStatus = "Least Concern", rarity = Rarity.COMMON
            ),
            Plant(
                id = "3", name = "Jasmine", scientificName = "Jasminum", family = "Oleaceae",
                description = "A fragrant flowering shrub or vine, popular for its scent.",
                habitat = "Gardens & warm climates",
                ecologicalImportance = "Why it matters: Feeds pollinators by scent alone and is woven into Tamil Nadu's culture and economy.\n\n" +
                        "Threats: Intensive commercial farming favors a few varieties, pushing out hardier local strains.\n\n" +
                        "Best practices: Grow heirloom jasmine varieties, not just market hybrids.\n\n" +
                        "How to help: Plant a jasmine vine at home — pollinators and your street will thank you.",
                conservationStatus = "Least Concern", rarity = Rarity.COMMON
            ),
            Plant(
                id = "4", name = "Neerium", scientificName = "Nerium oleander", family = "Apocynaceae",
                description = "An evergreen shrub with clusters of showy flowers.",
                habitat = "Indian subcontinent",
                ecologicalImportance = "Why it matters: A tough, drought-proof hedge plant that keeps pollinators fed even in dry spells.\n\n" +
                        "Threats: Often removed as \"just a roadside weed\" without knowing its ecological role.\n\n" +
                        "Best practices: Let it grow wild along boundaries instead of clearing it out.\n\n" +
                        "How to help: Admire, don't ingest, and spread awareness that it's toxic but valuable.",
                conservationStatus = "Least Concern", rarity = Rarity.NATIVE
            ),
            Plant(
                id = "5", name = "Red Pong Pong", scientificName = "Cerbera manghas", family = "Apocynaceae",
                description = "A flowering tree with red-eyed white blossoms and toxic fruit.",
                habitat = "Coastal regions",
                ecologicalImportance = "Why it matters: Anchors coastal and wetland ecosystems, preventing soil erosion.\n\n" +
                        "Threats: Wetlands are being drained and built over at an alarming rate.\n\n" +
                        "Best practices: Protect existing wetland patches instead of \"reclaiming\" them.\n\n" +
                        "How to help: Support local wetland clean-up drives; never handle the toxic seeds.",
                conservationStatus = "Least Concern", rarity = Rarity.COMMON
            ),
            Plant(
                id = "6", name = "Spiny Brinjal", scientificName = "Solanum virginianum", family = "Solanaceae",
                description = "A thorny herb used traditionally for its medicinal roots and fruit.",
                habitat = "Indian subcontinent",
                ecologicalImportance = "Why it matters: A go-to traditional medicinal plant used for generations in Indian households.\n\n" +
                        "Threats: Overharvesting for home remedies strips wild populations before they can seed.\n\n" +
                        "Best practices: Harvest a few leaves/fruits at a time; never uproot the whole plant.\n\n" +
                        "How to help: Let it grow in unused corners of your garden — it thrives on neglect!",
                conservationStatus = "Least Concern", rarity = Rarity.NATIVE
            ),
            Plant(
                id = "7", name = "Veldt Grape", scientificName = "Cissus quadrangularis", family = "Vitaceae",
                description = "A succulent climbing vine with distinctive four-angled stems.",
                habitat = "Indian subcontinent",
                ecologicalImportance = "Why it matters: A climbing succulent used in bone-health remedies and erosion control on slopes.\n\n" +
                        "Threats: Wild vines get uprooted for herbal medicine markets.\n\n" +
                        "Best practices: Propagate from cuttings instead of pulling wild stems.\n\n" +
                        "How to help: Grow a cutting on your balcony; it's nearly indestructible.",
                conservationStatus = "Least Concern", rarity = Rarity.NATIVE
            ),
            Plant(
                id = "8", name = "Velvet Beans", scientificName = "Mucuna pruriens", family = "Fabaceae",
                description = "A climbing legume covered in fine, irritant hairs on its pods.",
                habitat = "Tropical regions",
                ecologicalImportance = "Why it matters: Fixes nitrogen in soil naturally, improving land for other native plants too.\n\n" +
                        "Threats: Its fallow-land habitat is vanishing to construction and farming expansion.\n\n" +
                        "Best practices: Preserve fallow/wasteland patches instead of cleaning them up.\n\n" +
                        "How to help: Handle pods with care (they're itchy!) and let a patch grow undisturbed.",
                conservationStatus = "Least Concern", rarity = Rarity.NATIVE
            ),
            Plant(
                id = "9", name = "Wild Plumeria", scientificName = "Plumeria", family = "Apocynaceae",
                description = "A flowering tree known for its fragrant, waxy blossoms.",
                habitat = "Tropical gardens",
                ecologicalImportance = "Why it matters: A night-blooming favorite for moths, often found in temple groves.\n\n" +
                        "Threats: Landscaping trends favor imported ornamentals over native fragrant shrubs.\n\n" +
                        "Best practices: Propagate via cuttings and plant in shared/public green spaces.\n\n" +
                        "How to help: Ask your local nursery for native fragrant plants instead of exotics.",
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