package com.hdownloader.feature.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.layout.WindowMetricsCalculator
import com.hdownloader.core.designsystem.theme.HDTheme
import com.hdownloader.core.settings.AppSettingsRepository
import com.hdownloader.core.settings.ThemeMode
import com.hdownloader.feature.about.AboutScreen
import com.hdownloader.feature.browser.BrowserScreen
import com.hdownloader.feature.downloads.DownloadsScreen
import com.hdownloader.feature.home.HomeScreen
import com.hdownloader.feature.settings.SettingsScreen

/**
 * Root of the Compose UI. Applies the selected theme and hosts the adaptive
 * navigation scaffold with the primary navigation graph.
 */
@Composable
fun HDApp(
    settingsRepository: AppSettingsRepository,
) {
    val settings by settingsRepository.settings.collectAsState(initial = null)
    val themeMode = settings?.themeMode ?: ThemeMode.SYSTEM
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    HDTheme(
        darkTheme = darkTheme,
        dynamicColor = settings?.dynamicColor ?: false,
    ) {
        val navController = rememberNavController()
        val isWide = isWideWindow()

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (!isWide) {
                    HDNavigationBar(navController = navController)
                }
            },
        ) { innerPadding ->
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.padding(innerPadding),
            ) {
                if (isWide) {
                    HDNavigationRail(navController = navController)
                }
                NavHost(
                    navController = navController,
                    startDestination = HDRoutes.HOME,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                ) {
                    composable(HDRoutes.HOME) {
                        HomeScreen(onOpenDownloads = {
                            navController.navigate(HDRoutes.DOWNLOADS) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                            }
                        })
                    }
                    composable(HDRoutes.DOWNLOADS) {
                        DownloadsScreen()
                    }
                    composable(HDRoutes.BROWSER) {
                        BrowserScreen()
                    }
                    composable(HDRoutes.SETTINGS) {
                        SettingsScreen(onOpenAbout = {
                            navController.navigate(HDRoutes.ABOUT)
                        })
                    }
                    composable(HDRoutes.ABOUT) {
                        AboutScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}


@Composable
private fun isWideWindow(): Boolean {
    val context = LocalContext.current
    val metrics = WindowMetricsCalculator.getOrCreate()
        .computeCurrentWindowMetrics(context)
    val widthDp = metrics.bounds.width() / context.resources.displayMetrics.density
    return widthDp >= 600f
}

@Composable
private fun HDNavigationBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        TopLevelDestination.entries.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(text = stringResource(destination.labelRes))
                },
            )
        }
    }
}

@Composable
private fun HDNavigationRail(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        TopLevelDestination.entries.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationRailItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(text = stringResource(destination.labelRes))
                },
            )
        }
    }
}
