package com.muthuraj.cycle.fill.ui.recents

import androidx.compose.runtime.Stable
import com.muthuraj.cycle.fill.ui.items.Item
import com.muthuraj.cycle.fill.util.ViewState

@Stable
sealed interface RecentsScreenState : ViewState {
    data object Loading : RecentsScreenState
    data class Error(val message: String) : RecentsScreenState

    @Stable
    data class Success(
        val dates: RecentData
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
    val showCollectionName: Boolean,
    val number: String
)

data class RecentData(val categories: MutableList<RecentCategory>)

data class RecentCategory(val name: String, val subCategories: MutableList<RecentSubCategory>)

data class RecentSubCategory(val name: String, val collections: MutableList<RecentCollection>)

data class RecentCollection(val name: String, val items: MutableList<ItemDetailed>)