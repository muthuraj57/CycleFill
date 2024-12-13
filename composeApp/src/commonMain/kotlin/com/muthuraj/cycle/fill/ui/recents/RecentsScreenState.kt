package com.muthuraj.cycle.fill.ui.recents

import com.muthuraj.cycle.fill.ui.items.Item
import com.muthuraj.cycle.fill.util.ViewState

sealed interface RecentsScreenState : ViewState {
    data object Loading : RecentsScreenState
    data class Error(val message: String) : RecentsScreenState
    data class Success(
        val dates: List<Item>
    ) : RecentsScreenState
} 