package com.econova.econova.model

data class Plant(
    val id: String,
    val name: String,
    val scientificName: String,
    val description: String,
    val habitat: String,
    val ecologicalImportance: String,
    val conservationStatus: String,
    val rarity: Rarity,
    var isCaught: Boolean = false
)

enum class Rarity {
    COMMON, UNCOMMON, RARE, LEGENDARY
}
