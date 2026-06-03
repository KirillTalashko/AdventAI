package com.example.feature.chat.presentation.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.feature.chat.presentation.screen.ChatRoute

object ChatDestination {
    const val ROUTE = "chat"
}

fun NavGraphBuilder.chatScreen() {
    composable(route = ChatDestination.ROUTE) {
        ChatRoute(viewModel = hiltViewModel())
    }
}
