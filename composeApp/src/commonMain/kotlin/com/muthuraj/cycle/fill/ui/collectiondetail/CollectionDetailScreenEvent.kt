package com.muthuraj.cycle.fill.ui.collectiondetail

import com.muthuraj.cycle.fill.util.ViewEvent

sealed interface CollectionDetailScreenEvent : ViewEvent {
    data object AddDateClicked : CollectionDetailScreenEvent
    data class AddDate(val date: String) : CollectionDetailScreenEvent
    data class ShowDeleteConfirmation(val itemId: Int) : CollectionDetailScreenEvent
    data object ConfirmDelete : CollectionDetailScreenEvent
    data object DismissDeleteConfirmation : CollectionDetailScreenEvent
    data object DismissDialog : CollectionDetailScreenEvent
    data object Retry : CollectionDetailScreenEvent
    data class AddComment(val itemId: Int, val comment: String): CollectionDetailScreenEvent
} 