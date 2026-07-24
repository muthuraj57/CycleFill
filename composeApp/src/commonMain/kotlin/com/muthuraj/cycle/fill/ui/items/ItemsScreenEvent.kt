package com.muthuraj.cycle.fill.ui.items

import com.muthuraj.cycle.fill.ui.recents.RecentsScreenEvent
import com.muthuraj.cycle.fill.util.ViewEvent

sealed interface ItemsScreenEvent : ViewEvent {
    data object AddDateClicked : ItemsScreenEvent
    data class AddDate(val date: String) : ItemsScreenEvent
    data class ShowDeleteConfirmation(val itemId: Int) : ItemsScreenEvent
    data object ConfirmDelete : ItemsScreenEvent
    data object DismissDeleteConfirmation : ItemsScreenEvent
    data object DismissDialog : ItemsScreenEvent
    data object Retry : ItemsScreenEvent
    data class AddComment(val itemId: Int, val comment: String): ItemsScreenEvent
    data class Search(val searchText: String) : ItemsScreenEvent
    data object ShowExport : ItemsScreenEvent
    data object DismissExport : ItemsScreenEvent
    data class EnterSelectionMode(val itemId: Int) : ItemsScreenEvent
    data class ToggleSelection(val itemId: Int) : ItemsScreenEvent
    data object SelectUpToTop : ItemsScreenEvent
    data object SelectUpToBottom : ItemsScreenEvent
    data object ExitSelectionMode : ItemsScreenEvent
} 