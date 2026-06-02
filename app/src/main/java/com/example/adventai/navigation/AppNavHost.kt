package com.example.adventai.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.feature.chat.presentation.navigation.ChatDestination
import com.example.feature.chat.presentation.navigation.chatScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ChatDestination.ROUTE
    ) {
        chatScreen()
    }
}
