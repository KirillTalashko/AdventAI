package com.example.adventai.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.adventai.R
import com.example.adventai.ui.documents.DocumentsScreen
import com.example.adventai.ui.readiness.ReadinessScreen
import com.example.feature.chat.presentation.navigation.ChatDestination
import com.example.feature.chat.presentation.navigation.chatScreen
import com.example.feature.home.presentation.navigation.HomeDestination
import com.example.feature.home.presentation.navigation.homeScreen

private const val DocumentsRoute = "documents"
private const val ReadinessRoute = "readiness"

private data class BottomTab(
    val route: String,
    val iconRes: Int,
    val label: String
)

private val bottomTabs = listOf(
    BottomTab(HomeDestination.ROUTE, R.drawable.ic_tab_chat, "Чат"),
    BottomTab(DocumentsRoute, R.drawable.ic_tab_documents, "Документы"),
    BottomTab(ReadinessRoute, R.drawable.ic_tab_readiness, "Готовность")
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomTabs.map { it.route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
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
            composable(route = DocumentsRoute) { DocumentsScreen() }
            composable(route = ReadinessRoute) { ReadinessScreen() }
        }
    }
}

@Composable
private fun AppBottomBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        bottomTabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { if (currentRoute != tab.route) onTabSelected(tab.route) },
                icon = {
                    Icon(
                        painter = painterResource(id = tab.iconRes),
                        contentDescription = tab.label
                    )
                },
                label = { Text(text = tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
