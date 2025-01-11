package com.muthuraj.cycle.fill.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key.Companion.T
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.muthuraj.cycle.fill.di.AppComponent
import com.muthuraj.cycle.fill.di.create
import com.muthuraj.cycle.fill.navigation.Screen
import com.muthuraj.cycle.fill.network.NetworkManager
import com.muthuraj.cycle.fill.ui.collections.CollectionsScreen
import com.muthuraj.cycle.fill.ui.collections.CollectionsViewModel
import com.muthuraj.cycle.fill.ui.dashboard.DashboardScreen
import com.muthuraj.cycle.fill.ui.items.ItemsScreen
import com.muthuraj.cycle.fill.ui.recents.RecentsScreen
import com.muthuraj.cycle.fill.ui.recents.RecentsScreenEvent
import cyclefill.composeapp.generated.resources.Res
import cyclefill.composeapp.generated.resources.local
import cyclefill.composeapp.generated.resources.tailscale
import cyclefill.composeapp.generated.resources.visibility
import cyclefill.composeapp.generated.resources.visibility_off
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
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
            modifier = Modifier.fillMaxSize(),
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
            },
            topBar = {
                val currentBackStack by navController.currentBackStackEntryFlow.collectAsState(
                    initial = null
                )
                val viewModel = viewModel { appComponent.recentsViewModelProvider() }
                if (currentBackStack != null) {
                    val screen = currentBackStack!!.toScreen()
                    val topBarPrivacyIcon = when (screen) {
                        is Screen.Recents -> {
                            if (viewModel.isPrivacyEnabledFlow.collectAsState().value) {
                                TopBarPrivacyIcon.All
                            } else {
                                TopBarPrivacyIcon.Private
                            }
                        }

                        else -> TopBarPrivacyIcon.Hide
                    }
                    TopBar(
                        screen = screen,
                        topBarPrivacyIcon = topBarPrivacyIcon,
                        onBackClick = {
                            navController.navigateUp()
                        },
                        onPrivacyViewClick = { viewModel.setEvent(RecentsScreenEvent.PrivacyViewClicked) },
                        onSearch = { viewModel.setEvent(RecentsScreenEvent.Search(it)) }
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard(Screen.Dashboard.Type.Category),
                modifier = Modifier.padding(paddingValues)
            ) {
                composable<Screen.Dashboard> {
                    val viewModel =
                        viewModel { appComponent.dashboardScreenViewModelProvider(it.toRoute()) }
                    val screenState by viewModel.viewState.collectAsState()
                    DashboardScreen(screenState = screenState, doAction = viewModel::setEvent)
                }
                composable<Screen.Recents> {
                    val viewModel = viewModel { appComponent.recentsViewModelProvider() }
                    val screenState by viewModel.viewState.collectAsState()
                    RecentsScreen(screenState = screenState, doAction = viewModel::setEvent)
                }
                composable<Screen.Collections> {
                    val viewModel =
                        viewModel {
                            appComponent.collectionsViewModelProvider(
                                it.toRoute(),
                                it.savedStateHandle
                            )
                        }
                    val screenState by viewModel.viewState.collectAsState()
                    CollectionsScreen(screenState = screenState, doAction = viewModel::setEvent)
                }
                composable<Screen.Items> {
                    val viewModel =
                        viewModel { appComponent.itemsViewModelProvider(it.toRoute()) }
                    val screenState by viewModel.viewState.collectAsState()
                    ItemsScreen(
                        screenState = screenState,
                        doAction = viewModel::setEvent,
                        onDataUpdated = {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set(CollectionsViewModel.DATA_UPDATED_KEY, true)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    screen: Screen,
    topBarPrivacyIcon: TopBarPrivacyIcon,
    onBackClick: () -> Unit,
    onSearch: (text: String) -> Unit,
    onPrivacyViewClick: () -> Unit
) {
    val title = when (screen) {
        is Screen.Dashboard -> screen.categoryName ?: "Categories"
        is Screen.Collections -> screen.itemName
        is Screen.Items -> screen.collectionName
        Screen.Recents -> "Recent Entries"
    }
    val showBackArrow = when (screen) {
        is Screen.Dashboard -> screen.categoryId != null
        is Screen.Recents -> false
        else -> true
    }
    var showSearchBar by remember { mutableStateOf(false) }
    val showSearch = when (screen) {
        is Screen.Recents -> true
        else -> {
            showSearchBar = false
            false
        }
    }
    var isTailScaleSelected by remember { mutableStateOf(NetworkManager.useTailScaleUrl) }
    TopAppBar(
        title = {
            if (showSearchBar) {
                SearchField(onSearch)
            } else {
                Text(title)
            }
        },
        navigationIcon = if (showBackArrow) {
            {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            }
        } else null,
        actions = {
            if (showSearch) {
                if (showSearchBar) {
                    IconButton(onClick = {
                        showSearchBar = false
                        onSearch("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Search",
                        )
                    }
                } else {
                    IconButton(onClick = { showSearchBar = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                        )
                    }
                }
            }
            when (topBarPrivacyIcon) {
                TopBarPrivacyIcon.Private -> {
                    IconButton(onClick = onPrivacyViewClick) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(Res.drawable.visibility),
                            contentDescription = "Search",
                        )
                    }
                }

                TopBarPrivacyIcon.All -> {
                    IconButton(onClick = onPrivacyViewClick) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(Res.drawable.visibility_off),
                            contentDescription = "Search",
                        )
                    }
                }

                TopBarPrivacyIcon.Hide -> {
                    // Do nothing
                }
            }
            NetworkSwitchIcon(isTailScaleSelected, onSwitch = {
                isTailScaleSelected = !isTailScaleSelected
            })
        }
    )
}

@Composable
private fun SearchField(onSearch: (text: String) -> Unit) {
    var searchText by remember { mutableStateOf("") }
    TextField(
        colors = TextFieldDefaults.textFieldColors(cursorColor = Color.White),
        value = searchText,
        onValueChange = {
            searchText = it.trim()
            onSearch(searchText)
        },
        label = { Text("Search here", color = Color.White) }
    )
}

private enum class TopBarPrivacyIcon {
    Private, All, Hide
}

@Composable
private fun NetworkSwitchIcon(isTailScaleSelected: Boolean, onSwitch: () -> Unit) {
    val networkSwitchIcon = if (isTailScaleSelected) {
        Res.drawable.local
    } else {
        Res.drawable.tailscale
    }
    IconButton(onClick = {
        NetworkManager.useTailScaleUrl = !NetworkManager.useTailScaleUrl
        onSwitch()
    }) {
        Icon(
            painter = painterResource(networkSwitchIcon),
            contentDescription = "Switch",
        )
    }
}

private fun NavBackStackEntry.toScreen(): Screen {
    return when {
        destination.hasRoute<Screen.Dashboard>() -> {
            toRoute<Screen.Dashboard>()
        }

        destination.hasRoute<Screen.Recents>() -> {
            toRoute<Screen.Recents>()
        }

        destination.hasRoute<Screen.Collections>() -> {
            toRoute<Screen.Collections>()
        }

        destination.hasRoute<Screen.Items>() -> {
            toRoute<Screen.Items>()
        }

        else -> {
            throw IllegalArgumentException("Unknown route: ${destination.route}")
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
                is Screen.Collections -> {
                    navController.navigate(it)
                }

                is Screen.Dashboard -> {
                    when (it.type) {
                        Screen.Dashboard.Type.Category -> {
                            navController.navigate(it) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }

                        Screen.Dashboard.Type.SubCategory -> {
                            navController.navigate(it)
                        }
                    }
                }

                Screen.Recents -> {
                    navController.navigate(it) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                is Screen.Items -> {
                    navController.navigate(it)
                }
            }
        }
}