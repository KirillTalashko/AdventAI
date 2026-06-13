package com.example.adventai.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.feature.chat.presentation.navigation.ChatDestination
import com.example.feature.chat.presentation.navigation.chatScreen
import com.example.feature.home.presentation.navigation.HomeDestination
import com.example.feature.home.presentation.navigation.homeScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeDestination.ROUTE,
            modifier = Modifier.padding(innerPadding)
        ) {
            homeScreen(
                onOpenAgent = { agentId ->
                    navController.navigate(ChatDestination.createRoute(agentId))
                }
            )
            chatScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
