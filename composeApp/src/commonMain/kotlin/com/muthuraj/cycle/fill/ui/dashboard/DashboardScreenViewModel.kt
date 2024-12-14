/* $Id$ */
package com.muthuraj.cycle.fill.ui.dashboard

import androidx.lifecycle.viewModelScope
import com.muthuraj.cycle.fill.models.Category
import com.muthuraj.cycle.fill.navigation.NavigationManager
import com.muthuraj.cycle.fill.navigation.Screen
import com.muthuraj.cycle.fill.network.NetworkManager
import com.muthuraj.cycle.fill.util.BaseViewModel
import com.muthuraj.cycle.fill.util.log
import com.muthuraj.cycle.fill.util.printDebugStackTrace
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

/**
 * Created by Muthuraj on 07/12/24.
 */
@Inject
class DashboardScreenViewModel(
    private val navigationManager: NavigationManager,
    private val networkManager: NetworkManager,
    @Assisted private val dashboardScreen: Screen.Dashboard
) :
    BaseViewModel<DashboardScreenEvent, DashboardScreenState>() {

    override fun setInitialState(): DashboardScreenState {
        return DashboardScreenState.Loading
    }

    init {
        loadData()
    }

    private var job: Job? = null
    private fun loadData() {
        setState { DashboardScreenState.Loading }
        job?.cancel()
        job = viewModelScope.launch {
            when (dashboardScreen.type) {
                Screen.Dashboard.Type.Category -> {
                    val result = runCatching {
                        networkManager.getCategories()
                    }
                    if (result.isSuccess) {
                        val response = result.getOrThrow()
                        if (response.success) {
                            val categories = response.data!!
                                .map {
                                    Category(
                                        name = it.name,
                                        imageUrl = it.icon,
                                        id = it.id
                                    )
                                }
                            setState { DashboardScreenState.Success(categories) }
                        } else {
                            log { "Error loading item details: ${response.message}" }
                            setState {
                                DashboardScreenState.Error(response.message!!)
                            }
                        }
                    } else {
                        val error = result.exceptionOrNull()!!
                        error.printDebugStackTrace()
                        log { "Error loading item details: $error" }
                        setState {
                            DashboardScreenState.Error(error.message ?: "Failed to load categories")
                        }
                    }
                }

                Screen.Dashboard.Type.SubCategory -> {
                    val result = runCatching {
                        networkManager.getSubCategories(categoryId = dashboardScreen.categoryId!!)
                    }
                    if (result.isSuccess) {
                        val response = result.getOrThrow()
                        if (response.success) {
                            val categories = response.data!!
                                .map {
                                    Category(
                                        name = it.name,
                                        imageUrl = it.icon,
                                        id = it.id
                                    )
                                }
                            setState { DashboardScreenState.Success(categories) }
                        } else {
                            log { "Error loading item details: ${response.message}" }
                            setState {
                                DashboardScreenState.Error(response.message!!)
                            }
                        }
                    } else {
                        val error = result.exceptionOrNull()!!
                        error.printDebugStackTrace()
                        log { "Error loading item details: $error" }
                        setState {
                            DashboardScreenState.Error(error.message ?: "Failed to load categories")
                        }
                    }
                }
            }

        }
    }

    override fun handleEvents(event: DashboardScreenEvent) {
        when (event) {
            is DashboardScreenEvent.CategoryClicked -> {
                viewModelScope.launch {
                    val nextScreen = when (dashboardScreen.type) {
                        Screen.Dashboard.Type.Category -> {
                            Screen.Dashboard(
                                type = Screen.Dashboard.Type.SubCategory,
                                categoryId = event.category.id,
                                categoryName = event.category.name
                            )
                        }

                        Screen.Dashboard.Type.SubCategory -> {
                            Screen.Collections(
                                subCategoryId = event.category.id,
                                itemName = event.category.name
                            )
                        }
                    }
                    navigationManager.navigate(nextScreen)
                }
            }

            DashboardScreenEvent.Retry -> loadData()
        }
    }
}