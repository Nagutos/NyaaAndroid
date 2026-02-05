package com.nagutos.nyaaandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nagutos.nyaaandroid.ui.screens.detail.DetailScreen
import com.nagutos.nyaaandroid.ui.screens.home.HomeScreen
import com.nagutos.nyaaandroid.ui.screens.settings.SettingsScreen
import com.nagutos.nyaaandroid.ui.theme.NyaaAndroidTheme
import com.nagutos.nyaaandroid.utils.AppTheme
import com.nagutos.nyaaandroid.utils.ThemePreferences
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initializing preferences
        val themePreferences = ThemePreferences(applicationContext)

        setContent {
            // Read theme
            val currentTheme by themePreferences.themeFlow.collectAsState(initial = AppTheme.SYSTEM)

            val systemInDark = isSystemInDarkTheme()
            
            NyaaAndroidTheme(
                appTheme = currentTheme,
                darkThemeSystem = systemInDark
            ) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {

                    composable(
                        route = "home?query={query}",
                        arguments = listOf(
                            navArgument("query") {
                                defaultValue = ""
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val query = backStackEntry.arguments?.getString("query") ?: ""

                        HomeScreen(
                            navController = navController,
                            initialQuery = query,
                            onTorrentClick = { url ->
                                val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
                                navController.navigate("detail/$encodedUrl")
                            },
                            onSettingsClick = {
                                navController.navigate("settings")
                            }
                        )
                    }

                    composable("detail/{url}") { backStackEntry ->
                        val url = backStackEntry.arguments?.getString("url") ?: ""
                        DetailScreen(
                            url = url,
                            navController = navController
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            currentTheme = currentTheme,
                            themePreferences = themePreferences,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}