/* $Id$ */
package com.muthuraj.cycle.fill.ui

import com.muthuraj.cycle.fill.util.ViewEvent

/**
 * Created by Muthuraj on 08/12/24.
 */
sealed interface AppViewEvent: ViewEvent {
    data class BottomNavClick(val bottomNavItem: BottomNavItem) : AppViewEvent
}