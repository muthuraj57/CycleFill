/* $Id$ */
package com.muthuraj.cycle.fill.navigation

import com.muthuraj.cycle.fill.di.AppScope
import com.muthuraj.cycle.fill.util.log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import me.tatarka.inject.annotations.Inject

/**
 * Created by Muthuraj on 07/12/24.
 */
@AppScope
@Inject
class NavigationManager {
    private val screen = Channel<Screen>()
    val screenFlow = screen.receiveAsFlow()

    suspend fun navigate(screen: Screen) {
        log { "navigate() called with: screen = [$screen]" }
        this.screen.send(screen)
    }
}