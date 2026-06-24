package com.tivanco.hymaco.appUI.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tivanco.hymaco.appUI.CraneLogScreen
import com.tivanco.hymaco.appUI.logsScreen.LogsScreen
import com.tivanco.hymaco.appUI.mainScreen.HomeScreen
import com.tivanco.hymaco.appUI.settingsScreen.SettingsScreen
import com.tivanco.hymaco.appUI.supportScreen.SupportScreen
import com.tivanco.hymaco.dataClass.Screen
import com.tivanco.hymaco.viewModel.ConnectionViewModel
import com.tivanco.hymaco.viewModel.CraneControlViewModel
import com.tivanco.hymaco.viewModel.CraneLogViewModel
import com.tivanco.hymaco.viewModel.LogAndErrorViewModel
import com.tivanco.hymaco.viewModel.MainUiViewModel

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun AppNavigation(
    connectionViewModel: ConnectionViewModel,
    craneControlViewModel: CraneControlViewModel,
    logAndErrorViewModel: LogAndErrorViewModel,
    uiViewModel: MainUiViewModel,
    craneLogViewModel: CraneLogViewModel
) {
    val focusManager = LocalFocusManager.current
    val navController = rememberNavController()
    val animDuration = 700

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // HOME SCREEN
        composable(
            route = Screen.Home.route,
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animDuration)
                ) + fadeOut()
            },
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animDuration)
                ) + fadeIn()
            }
        ) {
            HomeScreen(
                navController = navController,
                connectionViewModel = connectionViewModel,
                craneControlViewModel = craneControlViewModel,
                uiViewModel = uiViewModel
            )
        }
        // SETTINGS SCREEN
        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animDuration)
                ) + fadeIn()
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animDuration)
                ) + fadeOut()
            }
        ) { backStackEntry ->
            SettingsScreen(
                navController = navController,
            )
        }
        // SUPPORT SCREEN
        composable(
            route = Screen.Support.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animDuration)
                ) + fadeIn()
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animDuration)
                ) + fadeOut()
            }
        ) { backStackEntry ->
            SupportScreen(
                focusManager = focusManager,
                navController = navController
            )
        }
        // LOGS SCREEN
        composable(
            route = Screen.Logs.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animDuration)
                ) + fadeIn()
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animDuration)
                ) + fadeOut()
            }
        ) { backStackEntry ->
            LogsScreen(
                navController = navController,
                craneControlViewModel = craneControlViewModel
            )
        }
        // EVENT LOGS SCREEN
        composable(
            route = Screen.AppLogs.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animDuration)
                ) + fadeIn()
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animDuration)
                ) + fadeOut()
            }
        ) { backStackEntry ->
            CraneLogScreen(craneLogViewModel)
        }
    }
}