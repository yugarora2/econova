package com.econova.econova.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "econova_prefs")

object PlantDataStore {
    private val CAUGHT_PLANT_IDS = stringSetPreferencesKey("caught_plant_ids")

    fun caughtIdsFlow(context: Context): Flow<Set<String>> =
        context.dataStore.data.map { prefs ->
            prefs[CAUGHT_PLANT_IDS] ?: emptySet()
        }

    suspend fun setCaught(context: Context, plantId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[CAUGHT_PLANT_IDS] ?: emptySet()
            prefs[CAUGHT_PLANT_IDS] = current + plantId
        }
    }
    suspend fun setUncaught(context: Context, plantId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[CAUGHT_PLANT_IDS] ?: emptySet()
            prefs[CAUGHT_PLANT_IDS] = current - plantId
        }
    }
}