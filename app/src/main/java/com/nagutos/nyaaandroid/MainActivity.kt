package com.nagutos.nyaaandroid

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.nagutos.nyaaandroid.ui.screens.home.HomeViewModel
import com.nagutos.nyaaandroid.ui.screens.home.HomeViewModelFactory
import com.nagutos.nyaaandroid.model.NyaaSite
import com.nagutos.nyaaandroid.ui.screens.settings.SettingsScreen
import com.nagutos.nyaaandroid.ui.theme.NyaaAndroidTheme
import com.nagutos.nyaaandroid.utils.AppTheme
import com.nagutos.nyaaandroid.utils.LocaleManager
import com.nagutos.nyaaandroid.utils.ThemePreferences
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nagutos.nyaaandroid.data.local.entity.FavoriteTorrent
import com.nagutos.nyaaandroid.ui.screens.favorites.FavoritesViewModel
import com.nagutos.nyaaandroid.ui.screens.favorites.FavoritesViewModelFactory
import java.net.URLEncoder

class MainActivity : ComponentActivity() {

    // Apply the saved in-app language before any resource is resolved.
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.applySavedLocale(newBase))
    }

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

            val favoritesViewModel: FavoritesViewModel = viewModel(
                factory = FavoritesViewModelFactory(application)
            )

            // Hoisted to the activity so the bottom nav can reset the search back to the
            // index (re-tapping the "Search" tab) using the same instance the Home screen shows.
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(application)
            )

            // Drive the active index from the saved preference; a change resets Home to the
            // fresh index for the new site (taxonomies differ between Nyaa and Sukebei).
            val currentSite by themePreferences.siteFlow.collectAsState(initial = NyaaSite.NYAA)
            LaunchedEffect(currentSite) {
                homeViewModel.onSiteChanged(currentSite)
            }
            val scope = rememberCoroutineScope()

            val favorites by favoritesViewModel.favoriteTorrents.collectAsState(
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
                        val currentRoute = currentDestination?.route?.split("?")?.first()
                        val isMainScreen = mainScreens.any { it.route == currentRoute }

                        if (isMainScreen) {
                            val onHome = currentRoute == Screen.Search.route
                            val onFavorites = currentRoute == Screen.Favorites.route

                            // Tapping a site tab persists the choice and shows its home list;
                            // tapping the site you're already on resets it to the recent index.
                            val selectSite: (NyaaSite) -> Unit = { targetSite ->
                                val alreadyHere = onHome && currentSite == targetSite
                                scope.launch { themePreferences.setSite(targetSite) }
                                if (alreadyHere) {
                                    homeViewModel.resetToIndex()
                                } else {
                                    navController.navigate(Screen.Search.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }

                            val navColors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                // Sukebei (left)
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.Whatshot, contentDescription = stringResource(R.string.nav_sukebei)) },
                                    label = { Text(stringResource(R.string.nav_sukebei)) },
                                    selected = onHome && currentSite == NyaaSite.SUKEBEI,
                                    colors = navColors,
                                    onClick = { selectSite(NyaaSite.SUKEBEI) }
                                )
                                // Nyaa (center)
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.nav_nyaa)) },
                                    label = { Text(stringResource(R.string.nav_nyaa)) },
                                    selected = onHome && currentSite == NyaaSite.NYAA,
                                    colors = navColors,
                                    onClick = { selectSite(NyaaSite.NYAA) }
                                )
                                // Favorites (right)
                                NavigationBarItem(
                                    icon = {
                                        BadgedBox(badge = {
                                            if (favoritesCount > 0) Badge { Text(favoritesCount.toString()) }
                                        }) {
                                            Icon(Screen.Favorites.icon, contentDescription = stringResource(Screen.Favorites.labelRes))
                                        }
                                    },
                                    label = { Text(stringResource(Screen.Favorites.labelRes)) },
                                    selected = onFavorites,
                                    colors = navColors,
                                    onClick = {
                                        navController.navigate(Screen.Favorites.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding()),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Search.route,
                            // Instant navigation: the default 700ms crossfade blocked input
                            // during the transition; None keeps the app responsive at all times.
                            enterTransition = { EnterTransition.None },
                            exitTransition = { ExitTransition.None },
                            popEnterTransition = { EnterTransition.None },
                            popExitTransition = { ExitTransition.None },
                            modifier = Modifier.fillMaxSize()
                        ) {
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
                                    viewModel = homeViewModel,
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
                                    },
                                    onSettingsClick = {
                                        navController.navigate("settings")
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
}