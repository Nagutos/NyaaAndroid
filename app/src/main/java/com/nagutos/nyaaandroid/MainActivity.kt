package com.nagutos.nyaaandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nagutos.nyaaandroid.ui.DetailScreen
import com.nagutos.nyaaandroid.ui.HomeScreen
import com.nagutos.nyaaandroid.ui.SettingsScreen
import com.nagutos.nyaaandroid.ui.theme.NyaaAndroidTheme
import com.nagutos.nyaaandroid.utils.AppTheme
import com.nagutos.nyaaandroid.utils.ThemePreferences
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialisation des préférences
        val themePreferences = ThemePreferences(applicationContext)

        setContent {
            // 2. On écoute le choix de l'utilisateur (Clair, Sombre, AMOLED, Système)
            val currentTheme by themePreferences.themeFlow.collectAsState(initial = AppTheme.SYSTEM)

            // 3. On vérifie si le système est en mode sombre (pour le cas "SYSTEM")
            val systemInDark = isSystemInDarkTheme()

            // 4. On applique le thème avec les bons paramètres
            // C'est ici que ça plantait : on utilise maintenant 'appTheme' et 'darkThemeSystem'
            NyaaAndroidTheme(
                appTheme = currentTheme,
                darkThemeSystem = systemInDark
            ) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {

                    // Route ACCUEIL
                    composable("home") {
                        HomeScreen(
                            onTorrentClick = { url ->
                                val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                                navController.navigate("detail/$encodedUrl")
                            },
                            onSettingsClick = {
                                navController.navigate("settings")
                            }
                        )
                    }

                    // Route DÉTAIL
                    composable("detail/{torrentUrl}") { backStackEntry ->
                        val url = backStackEntry.arguments?.getString("torrentUrl") ?: ""
                        DetailScreen(url = url)
                    }

                    // Route PARAMÈTRES
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