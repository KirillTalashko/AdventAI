package com.example.feature.chat.presentation.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.feature.chat.presentation.format.screen.FormatRoute
import com.example.feature.chat.presentation.modelcomparison.screen.ModelComparisonRoute
import com.example.feature.chat.presentation.reasoning.screen.ReasoningRoute
import com.example.feature.chat.presentation.screen.ChatRoute
import com.example.feature.chat.presentation.temperature.screen.TemperatureRoute

object ChatDestination {
    const val ROUTE = "chat"
}

object Day2FormatDestination {
    const val ROUTE = "day2_format"
}

object ReasoningDestination {
    const val ROUTE = "reasoning"
}

object TemperatureDestination {
    const val ROUTE = "temperature"
}

object ModelComparisonDestination {
    const val ROUTE = "model_comparison"
}

fun NavGraphBuilder.chatScreen() {
    composable(route = ChatDestination.ROUTE) {
        ChatRoute(viewModel = hiltViewModel())
    }
}

fun NavGraphBuilder.day2FormatScreen() {
    composable(route = Day2FormatDestination.ROUTE) {
        FormatRoute(viewModel = hiltViewModel())
    }
}

fun NavGraphBuilder.reasoningScreen() {
    composable(route = ReasoningDestination.ROUTE) {
        ReasoningRoute(viewModel = hiltViewModel())
    }
}

fun NavGraphBuilder.temperatureScreen() {
    composable(route = TemperatureDestination.ROUTE) {
        TemperatureRoute(viewModel = hiltViewModel())
    }
}

fun NavGraphBuilder.modelComparisonScreen() {
    composable(route = ModelComparisonDestination.ROUTE) {
        ModelComparisonRoute(viewModel = hiltViewModel())
    }
}
