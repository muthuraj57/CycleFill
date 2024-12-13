package com.muthuraj.cycle.fill.ui.collections

import com.muthuraj.cycle.fill.models.Collection
import com.muthuraj.cycle.fill.util.ViewEvent

sealed interface CollectionsScreenEvent : ViewEvent {
    data object AddCollectionClicked : CollectionsScreenEvent
    data class AddCollection(val name: String) : CollectionsScreenEvent
    data class ShowDeleteConfirmation(val collection: Collection) : CollectionsScreenEvent
    data object ConfirmDelete : CollectionsScreenEvent
    data object DismissDeleteConfirmation : CollectionsScreenEvent
    data class CollectionClicked(val collection: Collection) : CollectionsScreenEvent
    data object DismissDialog : CollectionsScreenEvent
    data object Retry : CollectionsScreenEvent
} 