package com.example.feature.home.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.feature.home.presentation.screen.HomeScreen

object HomeDestination {
    const val ROUTE = "home"
}

fun NavGraphBuilder.homeScreen(
    onOpenAgent: (String) -> Unit
) {
    composable(route = HomeDestination.ROUTE) {
        HomeScreen(
            onOpenAgent = onOpenAgent,
            visaAgentId = "visa",
            newAgentId = "new"
        )
    }
}
