package com.example.adventai.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.adventai.R
import com.example.core.designsystem.component.AppScaffold
import com.example.feature.chat.presentation.navigation.ChatDestination
import com.example.feature.chat.presentation.navigation.Day2FormatDestination
import com.example.feature.chat.presentation.navigation.ModelComparisonDestination
import com.example.feature.chat.presentation.navigation.ReasoningDestination
import com.example.feature.chat.presentation.navigation.TemperatureDestination
import com.example.feature.chat.presentation.navigation.chatScreen
import com.example.feature.chat.presentation.navigation.day2FormatScreen
import com.example.feature.chat.presentation.navigation.modelComparisonScreen
import com.example.feature.chat.presentation.navigation.reasoningScreen
import com.example.feature.chat.presentation.navigation.temperatureScreen
import com.example.feature.home.presentation.navigation.HomeDestination
import com.example.feature.home.presentation.navigation.homeScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: HomeDestination.ROUTE

    AppScaffold(
        title = stringResource(R.string.app_name),
        onNavigateBack = if (currentRoute == HomeDestination.ROUTE) {
            null
        } else {
            {
                navController.navigateUp()
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeDestination.ROUTE,
            modifier = Modifier.padding(innerPadding)
        ) {
            homeScreen(
                onOpenChat = {
                    navController.navigate(ChatDestination.ROUTE)
                },
                onOpenDay2Format = {
                    navController.navigate(Day2FormatDestination.ROUTE)
                },
                onOpenDay3Reasoning = {
                    navController.navigate(ReasoningDestination.ROUTE)
                },
                onOpenDay4Temperature = {
                    navController.navigate(TemperatureDestination.ROUTE)
                },
                onOpenDay5Models = {
                    navController.navigate(ModelComparisonDestination.ROUTE)
                }
            )
            chatScreen()
            day2FormatScreen()
            reasoningScreen()
            temperatureScreen()
            modelComparisonScreen()
        }
    }
}
