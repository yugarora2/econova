package com.econova.econova.data

import com.econova.econova.model.Plant
import com.econova.econova.model.Rarity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

object PlantRepository {
    private val _plants = MutableStateFlow(
        listOf(
            Plant(
                "1", "Candi-leaf", "Stevia rebaudiana", 
                "A sweet leaf plant known for its natural sweetness.", 
                "Tropical/Subtropical", "Natural sweetener source.", "Vulnerable", Rarity.UNCOMMON
            ),
            Plant(
                "2", "Neem", "Azadirachta indica", 
                "A tree in the mahogany family with medicinal properties.", 
                "Drier regions", "Natural pesticide and medicine.", "Least Concern", Rarity.COMMON
            ),
            Plant(
                "3", "Holy Basil", "Ocimum tenuiflorum", 
                "An aromatic perennial plant known as Tulsi.", 
                "Indian subcontinent", "Cultural and medicinal significance.", "Least Concern", Rarity.COMMON
            ),
            Plant(
                "4", "Golden Shower", "Cassia fistula", 
                "A flowering plant with bright yellow pendulous flowers.", 
                "Tropical forests", "Attracts bees and butterflies.", "Least Concern", Rarity.UNCOMMON
            ),
            Plant(
                "5", "Banyan", "Ficus benghalensis", 
                "A large tree that starts its life as an epiphyte.", 
                "Indian subcontinent", "A miniature ecosystem in itself.", "Least Concern", Rarity.RARE
            ),
            Plant(
                "6", "Peepal", "Ficus religiosa", 
                "A species of fig native to the Indian subcontinent.", 
                "Indo-Gangetic Plain", "Significant oxygen producer.", "Least Concern", Rarity.COMMON
            ),
            Plant(
                "7", "Amla", "Phyllanthus emblica", 
                "The Indian gooseberry, famous for its Vitamin C.", 
                "Tropical & Subtropical", "Highly medicinal and edible.", "Data Deficient", Rarity.RARE
            )
        )
    )

    val plants = _plants.asStateFlow()
    val caughtPlants = plants.map { it.filter { p -> p.isCaught } }

    fun catchPlant(id: String) {
        val currentList = _plants.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isCaught = true)
            _plants.value = currentList
        }
    }
    
    fun getPlant(id: String): Plant? = _plants.value.find { it.id == id }
}
