package com.hdownloader.feature.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument

object HDRoutes {
    const val HOME = "home"
    const val DOWNLOADS = "downloads"
    const val BROWSER = "browser"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val ADD_URL = "add_url"
}

enum class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    HOME(
        route = HDRoutes.HOME,
        labelRes = com.hdownloader.R.string.nav_home,
        icon = Icons.Rounded.Home,
    ),
    DOWNLOADS(
        route = HDRoutes.DOWNLOADS,
        labelRes = com.hdownloader.R.string.nav_downloads,
        icon = Icons.Rounded.Download,
    ),
    BROWSER(
        route = HDRoutes.BROWSER,
        labelRes = com.hdownloader.R.string.nav_browser,
        icon = Icons.Rounded.Public,
    ),
    SETTINGS(
        route = HDRoutes.SETTINGS,
        labelRes = com.hdownloader.R.string.nav_settings,
        icon = Icons.Rounded.Settings,
    ),
}
