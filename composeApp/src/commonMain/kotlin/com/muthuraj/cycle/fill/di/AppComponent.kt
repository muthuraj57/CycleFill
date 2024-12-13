/* $Id$ */
package com.muthuraj.cycle.fill.di

import androidx.navigation.NavController
import com.muthuraj.cycle.fill.navigation.NavigationManager
import com.muthuraj.cycle.fill.navigation.Screen
import com.muthuraj.cycle.fill.ui.AppViewModel
import com.muthuraj.cycle.fill.ui.items.ItemsViewModel
import com.muthuraj.cycle.fill.ui.dashboard.DashboardScreenViewModel
import com.muthuraj.cycle.fill.ui.collections.CollectionsViewModel
import com.muthuraj.cycle.fill.ui.recents.RecentsViewModel
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

/**
 * Created by Muthuraj on 07/12/24.
 */
@AppScope
@Component
abstract class AppComponent(
    @get:Provides val navController: NavController
) {
    abstract val navigationManager: NavigationManager

    abstract val dashboardScreenViewModelProvider: (Screen.Dashboard) -> DashboardScreenViewModel

    abstract val appViewModelProvider: () -> AppViewModel

    abstract val collectionsViewModelProvider: (Screen.Collections) -> CollectionsViewModel

    abstract val itemsViewModelProvider: (Screen.Items) -> ItemsViewModel
    
    abstract val recentsViewModelProvider: () -> RecentsViewModel
}