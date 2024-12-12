/* $Id$ */
package com.muthuraj.cycle.fill.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector
import com.muthuraj.cycle.fill.util.ViewState

/**
 * Created by Muthuraj on 08/12/24.
 */
data class AppViewState(
    val currentBottomNavItem: BottomNavItem,
    val bottomNavItems: List<BottomNavItem>
) : ViewState {
}

enum class BottomNavItem(
    val icon: ImageVector,
    val label: String
) {
    Dashboard(Icons.Default.Home, "Dashboard"),
    Recents(Icons.Default.List, "Recents")
}