package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.AppNavigationDrawerSheet
import com.example.ui.navigation.Routes
import com.example.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun CollectorApp(
    viewModel: CollectorViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val bottomNavRoutes = listOf(
        Routes.DASHBOARD,
        Routes.COLLECTION,
        Routes.SCANNER,
        Routes.DECKS_LIST,
        Routes.STATISTICS
    )

    val showBottomBar = currentRoute in bottomNavRoutes

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppNavigationDrawerSheet(
                navController = navController,
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentRoute == Routes.DASHBOARD,
                            onClick = {
                                navController.navigate(Routes.DASHBOARD) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Início") },
                            label = { Text("Início") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == Routes.COLLECTION,
                            onClick = {
                                navController.navigate(Routes.COLLECTION) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Style, contentDescription = "Coleção") },
                            label = { Text("Coleção") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == Routes.SCANNER,
                            onClick = {
                                navController.navigate(Routes.SCANNER) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear") },
                            label = { Text("Escanear") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == Routes.DECKS_LIST,
                            onClick = {
                                navController.navigate(Routes.DECKS_LIST) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.FolderSpecial, contentDescription = "Decks") },
                            label = { Text("Decks") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == Routes.STATISTICS,
                            onClick = {
                                navController.navigate(Routes.STATISTICS) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
                            label = { Text("Stats") }
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.DASHBOARD,
                modifier = Modifier.padding(padding)
            ) {
                composable(Routes.DASHBOARD) {
                    DashboardScreen(
                        viewModel = viewModel,
                        navController = navController,
                        onOpenDrawer = {
                            coroutineScope.launch { drawerState.open() }
                        }
                    )
                }
                composable(Routes.COLLECTION) {
                    CollectionScreen(
                        viewModel = viewModel,
                        navController = navController,
                        onOpenDrawer = {
                            coroutineScope.launch { drawerState.open() }
                        }
                    )
                }
                composable(Routes.SCANNER) {
                    ScannerScreen(viewModel, navController)
                }
                composable(Routes.SCAN_RESULT) {
                    ScanResultScreen(viewModel, navController)
                }
                composable(Routes.STATISTICS) {
                    StatisticsScreen(
                        viewModel = viewModel,
                        navController = navController,
                        onOpenDrawer = {
                            coroutineScope.launch { drawerState.open() }
                        }
                    )
                }
                composable(Routes.ADD_ITEM) {
                    AddItemScreen(viewModel, navController)
                }
                composable(
                    route = Routes.ITEM_DETAIL,
                    arguments = listOf(navArgument("itemId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
                    ItemDetailScreen(viewModel, navController, itemId)
                }
                composable(Routes.DECKS_LIST) {
                    DecksListScreen(
                        viewModel = viewModel,
                        navController = navController,
                        onOpenDrawer = {
                            coroutineScope.launch { drawerState.open() }
                        }
                    )
                }
                composable(
                    route = Routes.DECK_DETAIL,
                    arguments = listOf(navArgument("deckId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val deckId = backStackEntry.arguments?.getInt("deckId") ?: 0
                    DeckDetailScreen(viewModel, navController, deckId)
                }
                composable(Routes.WISHLIST) {
                    WishlistScreen(viewModel, navController)
                }
                composable(Routes.MARKETPLACE) {
                    MarketplaceScreen(viewModel, navController)
                }
                composable(Routes.COMPARE) {
                    CompareScreen(viewModel, navController)
                }
                composable(Routes.ACHIEVEMENTS) {
                    AchievementsScreen(viewModel, navController)
                }
                composable(Routes.MISSING_CARDS) {
                    MissingCardsScreen(viewModel, navController)
                }
                composable(Routes.PHYSICAL_INVENTORY) {
                    PhysicalInventoryScreen(viewModel, navController)
                }
                composable(Routes.DUPLICATES) {
                    DuplicatesScreen(viewModel, navController)
                }
                composable(Routes.AI_ASSISTANT) {
                    AiAssistantScreen(viewModel, navController)
                }
                composable(Routes.PRICE_ALERTS) {
                    PriceAlertsScreen(viewModel, navController)
                }
                composable(Routes.PATRIMONY_BACKUP) {
                    PatrimonyBackupScreen(viewModel, navController)
                }
                composable(Routes.SEALED_PRODUCTS) {
                    SealedProductsScreen(viewModel, navController)
                }
                composable(Routes.EFFECTS_GUIDE) {
                    EffectsGuideScreen(viewModel, navController)
                }
                composable(Routes.IMPORT_CATALOG) {
                    ImportCatalogScreen(viewModel, navController)
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(viewModel, navController)
                }
            }
        }
    }
}
