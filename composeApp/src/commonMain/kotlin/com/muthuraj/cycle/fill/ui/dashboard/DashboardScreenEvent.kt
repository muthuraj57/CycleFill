/* $Id$ */
package com.muthuraj.cycle.fill.ui.dashboard

import com.muthuraj.cycle.fill.models.Category
import com.muthuraj.cycle.fill.util.ViewEvent

/**
 * Created by Muthuraj on 08/12/24.
 */
sealed interface DashboardScreenEvent: ViewEvent {
    data class CategoryClicked(val category: Category): DashboardScreenEvent
    data object Retry: DashboardScreenEvent
}