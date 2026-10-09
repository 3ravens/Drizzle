package dev.drizzle.weather.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.drizzle.weather.ui.overview.OverviewScreen
import dev.drizzle.weather.ui.settings.SettingsScreen

object Routes {
    const val OVERVIEW = "overview"
    const val SETTINGS = "settings"
}

@Composable
fun DrizzleNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.OVERVIEW) {
        composable(Routes.OVERVIEW) {
            OverviewScreen(
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
