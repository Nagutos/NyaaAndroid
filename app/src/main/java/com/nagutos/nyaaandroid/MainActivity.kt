package com.nagutos.nyaaandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.nagutos.nyaaandroid.ui.screens.Screen
import com.nagutos.nyaaandroid.ui.screens.detail.DetailScreen
import com.nagutos.nyaaandroid.ui.screens.favorites.FavoritesScreen
import com.nagutos.nyaaandroid.ui.screens.home.HomeScreen
import com.nagutos.nyaaandroid.ui.screens.settings.SettingsScreen
import com.nagutos.nyaaandroid.ui.theme.NyaaAndroidTheme
import com.nagutos.nyaaandroid.utils.AppTheme
import com.nagutos.nyaaandroid.utils.ThemePreferences
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nagutos.nyaaandroid.data.local.entity.FavoriteTorrent
import com.nagutos.nyaaandroid.ui.screens.home.HomeViewModel
import com.nagutos.nyaaandroid.ui.screens.home.HomeViewModelFactory
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themePreferences = ThemePreferences(applicationContext)

        setContent {
            val currentTheme by themePreferences.themeFlow.collectAsState(initial = AppTheme.SYSTEM)
            val systemInDark = isSystemInDarkTheme()

            val navController = rememberNavController()

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            val mainViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(application)
            )

            val favorites by mainViewModel.favoriteTorrents.collectAsState(
                initial = emptyList<FavoriteTorrent>()
            )
            val favoritesCount = favorites.size

            val mainScreens = listOf(Screen.Search, Screen.Favorites)

            NyaaAndroidTheme(
                appTheme = currentTheme,
                darkThemeSystem = systemInDark
            ) {
                Scaffold(
                    bottomBar = {
                        val isMainScreen = mainScreens.any { it.route == currentDestination?.route?.split("?")?.first() }

                        if (isMainScreen) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp) {
                                mainScreens.forEach { screen ->
                                    NavigationBarItem(
                                        icon = {
                                            BadgedBox(
                                                badge = {
                                                    if (screen == Screen.Favorites && favoritesCount > 0) {
                                                        Badge {
                                                            Text(favoritesCount.toString())
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(screen.icon, contentDescription = screen.label)
                                            }
                                        },
                                        label = { Text(screen.label) },
                                        selected = currentDestination?.hierarchy?.any { it.route?.startsWith(screen.route) == true } == true,
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        onClick = {
                                            navController.navigate(screen.route) {
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
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Search.route,
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                        enterTransition = { fadeIn(animationSpec = tween(300)) },
                        exitTransition = { fadeOut(animationSpec = tween(300)) }
                    ){
                        // --- Home ---
                        composable(
                            route = Screen.Search.route + "?query={query}",
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
                                    val encodedUrl = URLEncoder.encode(url, "UTF-8")
                                    navController.navigate("detail/$encodedUrl")
                                },
                                onSettingsClick = {
                                    navController.navigate("settings")
                                }
                            )
                        }

                        // --- Favorite Screen ---
                        composable(Screen.Favorites.route) {
                            FavoritesScreen(
                                navController = navController,
                                onTorrentClick = { url ->
                                    val encodedUrl = URLEncoder.encode(url, "UTF-8")
                                    navController.navigate("detail/$encodedUrl")
                                }
                            )
                        }

                        // --- Details Screen ---
                        composable("detail/{url}") { backStackEntry ->
                            val url = backStackEntry.arguments?.getString("url") ?: ""
                            DetailScreen(
                                url = url,
                                navController = navController
                            )
                        }

                        // --- ÉCRAN PARAMÈTRES ---
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
}