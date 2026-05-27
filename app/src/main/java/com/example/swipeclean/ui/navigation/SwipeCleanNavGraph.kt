package com.example.swipeclean.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.swipeclean.ui.analytics.AnalyticsRoute
import com.example.swipeclean.ui.bin.BinRoute
import com.example.swipeclean.ui.home.HomeRoute
import com.example.swipeclean.ui.onboarding.OnboardingRoute
import com.example.swipeclean.ui.preview.PreviewRoute
import com.example.swipeclean.ui.settings.SettingsRoute
import com.example.swipeclean.ui.swipe.SwipeRoute

@Composable
fun SwipeCleanNavGraph(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Destinations.ONBOARDING) {
            OnboardingRoute(
                onFinished = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Destinations.HOME) {
            HomeRoute(
                onStartCleaning = { navController.navigate(Destinations.SWIPE) },
                onOpenBin = { navController.navigate(Destinations.BIN) },
                onOpenAnalytics = { navController.navigate(Destinations.ANALYTICS) },
                onOpenSettings = { navController.navigate(Destinations.SETTINGS) }
            )
        }
        composable(Destinations.SWIPE) {
            SwipeRoute(
                onBack = { navController.popBackStack() },
                onOpenPreview = { mediaId ->
                    navController.navigate(Destinations.preview(mediaId))
                }
            )
        }
        composable(Destinations.BIN) {
            BinRoute(onBack = { navController.popBackStack() })
        }
        composable(
            Destinations.PREVIEW,
            arguments = listOf(navArgument("mediaId") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("mediaId") ?: -1L
            PreviewRoute(
                mediaId = id,
                onClose = { navController.popBackStack() }
            )
        }
        composable(Destinations.ANALYTICS) {
            AnalyticsRoute(onBack = { navController.popBackStack() })
        }
        composable(Destinations.SETTINGS) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }
    }
}
