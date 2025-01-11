/* $Id$ */
package com.muthuraj.cycle.fill.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
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
    Recents(Icons.AutoMirrored.Filled.List, "Recents"),
    Settings(Icons.Default.Settings, "Settings"),
}