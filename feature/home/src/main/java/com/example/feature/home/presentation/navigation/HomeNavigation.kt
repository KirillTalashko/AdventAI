package com.example.feature.home.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.feature.home.presentation.screen.HomeScreen

object HomeDestination {
    const val ROUTE = "home"
}

fun NavGraphBuilder.homeScreen(
    onOpenChat: () -> Unit,
    onOpenDay2Format: () -> Unit,
    onOpenDay3Reasoning: () -> Unit,
    onOpenDay4Temperature: () -> Unit
) {
    composable(route = HomeDestination.ROUTE) {
        HomeScreen(
            onOpenChat = onOpenChat,
            onOpenDay2Format = onOpenDay2Format,
            onOpenDay3Reasoning = onOpenDay3Reasoning,
            onOpenDay4Temperature = onOpenDay4Temperature
        )
    }
}
