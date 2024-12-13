package com.muthuraj.cycle.fill.ui.recents

import com.muthuraj.cycle.fill.ui.items.Item
import com.muthuraj.cycle.fill.util.ViewState

sealed interface RecentsScreenState : ViewState {
    data object Loading : RecentsScreenState
    data class Error(val message: String) : RecentsScreenState
    data class Success(
        val dates: List<ItemDetailed>
    ) : RecentsScreenState
}

data class ItemDetailed(
    val id: Int,
    val date: String,
    val weekDay: String,
    val daysAgoForLastCycle: Pair<Int, String>?,
    val timestamp: String,
    val comment: String?,
    val categoryName: String,
    val subCategoryName: String,
    val collectionName: String,
    val showCategoryName: Boolean,
    val showSubCategoryName: Boolean,
    val showCollectionName: Boolean
)