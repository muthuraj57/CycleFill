package com.muthuraj.cycle.fill.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.muthuraj.cycle.fill.di.AppComponent
import com.muthuraj.cycle.fill.di.create
import com.muthuraj.cycle.fill.navigation.Screen
import com.muthuraj.cycle.fill.ui.dashboard.DashboardScreen
import com.muthuraj.cycle.fill.ui.itemdetail.ItemDetailScreen
import com.muthuraj.cycle.fill.ui.recents.RecentsScreen
import com.muthuraj.cycle.fill.ui.collectiondetail.CollectionDetailScreen
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        val appComponent = remember(navController) {
            AppComponent::class.create(navController)
        }

        val appViewModel = viewModel { appComponent.appViewModelProvider() }

        LaunchedEffect(Unit) {
            observeNavigation(appViewModel.screenFlow, navController)
        }

        val appScreenState by appViewModel.viewState.collectAsState()

        Scaffold(
            bottomBar = {
                BottomNavigation {
                    appScreenState.bottomNavItems.forEach { item ->
                        BottomNavigationItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = item == appScreenState.currentBottomNavItem,
                            onClick = {
                                appViewModel.setEvent(AppViewEvent.BottomNavClick(item))
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable<Screen.Dashboard> {
                    val viewModel = viewModel { appComponent.dashboardScreenViewModelProvider() }
                    val screenState by viewModel.viewState.collectAsState()
                    DashboardScreen(screenState = screenState, doAction = viewModel::setEvent)
                }
                composable<Screen.Recents> {
                    RecentsScreen()
                }
                composable<Screen.ItemDetail> {
                    val viewModel =
                        viewModel { appComponent.itemDetailViewModelProvider(it.toRoute()) }
                    val screenState by viewModel.viewState.collectAsState()
                    ItemDetailScreen(screenState = screenState, doAction = viewModel::setEvent)
                }
                composable<Screen.CollectionDetail> {
                    val viewModel = viewModel { appComponent.collectionDetailViewModelProvider(it.toRoute()) }
                    val screenState by viewModel.viewState.collectAsState()
                    CollectionDetailScreen(screenState = screenState, doAction = viewModel::setEvent)
                }
            }
        }
    }
}

private suspend fun observeNavigation(
    screenFlow: Flow<Screen>,
    navController: NavHostController
) {
    screenFlow
        .collect {
            when (it) {
                is Screen.ItemDetail -> {
                    navController.navigate(it)
                }

                Screen.Dashboard, Screen.Recents -> {
                    navController.navigate(it) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                is Screen.CollectionDetail -> {
                    navController.navigate(it)
                }
            }
        }
}