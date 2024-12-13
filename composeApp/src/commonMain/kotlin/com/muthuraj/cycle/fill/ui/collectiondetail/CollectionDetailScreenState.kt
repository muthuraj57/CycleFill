package com.muthuraj.cycle.fill.ui.collectiondetail

import com.muthuraj.cycle.fill.util.ViewState
import dev.gitlive.firebase.firestore.Timestamp

sealed interface CollectionDetailScreenState : ViewState {
    data object Loading : CollectionDetailScreenState
    data class Error(val message: String) : CollectionDetailScreenState
    data class Success(
        val collectionName: String,
        val dates: List<CollectionDetailItem>,
        val showAddDialog: Boolean = false,
        val deleteConfirmation: Int? = null
    ) : CollectionDetailScreenState
}

data class CollectionDetailItem(
    val id: Int,
    val date: String,
    val weekDay: String,
    val daysAgoForLastCycle: Pair<Int, String>?,
    val timestamp: String,
    val comment: String?
)