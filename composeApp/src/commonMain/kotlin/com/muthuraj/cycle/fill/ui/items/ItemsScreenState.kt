package com.muthuraj.cycle.fill.ui.items

import com.muthuraj.cycle.fill.util.ViewState

sealed interface ItemsScreenState : ViewState {
    data object Loading : ItemsScreenState
    data class Error(val message: String) : ItemsScreenState
    data class Success(
        val collectionName: String,
        val dates: List<Item>,
        val showAddDialog: Boolean = false,
        val deleteConfirmation: Int? = null
    ) : ItemsScreenState
}

data class Item(
    val id: Int,
    val date: String,
    val weekDay: String,
    val daysAgoForLastCycle: Pair<Int, String>?,
    val timestamp: String,
    val comment: String?
)