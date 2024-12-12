/* $Id$ */
package com.muthuraj.cycle.fill.ui.dashboard

import com.muthuraj.cycle.fill.models.Category
import com.muthuraj.cycle.fill.util.ViewState

/**
 * Created by Muthuraj on 08/12/24.
 */
sealed interface DashboardScreenState: ViewState {
    data object Loading: DashboardScreenState
    data class Error(val message: String): DashboardScreenState
    data class Success(val categories: List<Category>): DashboardScreenState
}

