package com.econova.econova

import android.app.Application
import com.econova.econova.data.PlantRepository

class EconovaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PlantRepository.init(this)
    }
}