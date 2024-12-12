/* $Id$ */
package com.muthuraj.cycle.fill.ui

import androidx.lifecycle.viewModelScope
import com.muthuraj.cycle.fill.navigation.NavigationManager
import com.muthuraj.cycle.fill.navigation.Screen
import com.muthuraj.cycle.fill.util.BaseViewModel
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

/**
 * Created by Muthuraj on 08/12/24.
 */
@Inject
class AppViewModel(private val navigationManager: NavigationManager) :
    BaseViewModel<AppViewEvent, AppViewState>() {
    val screenFlow = navigationManager.screenFlow

    override fun setInitialState(): AppViewState {
        return AppViewState(
            currentBottomNavItem = BottomNavItem.Dashboard,
            bottomNavItems = BottomNavItem.entries
        )
    }

    override fun handleEvents(event: AppViewEvent) {
        viewModelScope.launch {
            when (event) {
                is AppViewEvent.BottomNavClick -> {
                    when (event.bottomNavItem) {
                        BottomNavItem.Dashboard -> {
                            setState { copy(currentBottomNavItem = event.bottomNavItem) }
                            navigationManager.navigate(Screen.Dashboard)
                        }

                        BottomNavItem.Recents -> {
                            setState { copy(currentBottomNavItem = event.bottomNavItem) }
                            navigationManager.navigate(Screen.Recents)
                        }
                    }
                }
            }
        }
    }
}