package com.econova.econova

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.econova.econova.ui.InventoryScreen
import com.econova.econova.ui.MainScreen
import com.econova.econova.ui.PlantDetailScreen
import com.econova.econova.ui.theme.EconovaTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Handle permission denied
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            EconovaTheme {
                EconovaApp()
            }
        }
    }
}

@Composable
fun EconovaApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            if (currentDestination?.route != "detail/{plantId}") {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Camera, contentDescription = "Scan") },
                        label = { Text("Scan") },
                        selected = currentDestination?.hierarchy?.any { it.route == "scan" } == true,
                        onClick = {
                            navController.navigate("scan") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Inventory, contentDescription = "Pokedex") },
                        label = { Text("Pokedex") },
                        selected = currentDestination?.hierarchy?.any { it.route == "inventory" } == true,
                        onClick = {
                            navController.navigate("inventory") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "scan",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("scan") { MainScreen(navController) }
            composable("inventory") { InventoryScreen(navController) }
            composable("detail/{plantId}") { backStackEntry ->
                val plantId = backStackEntry.arguments?.getString("plantId")
                PlantDetailScreen(plantId, navController)
            }
        }
    }
}
