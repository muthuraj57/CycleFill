package com.muthuraj.cycle.fill.ui.itemdetail

import com.muthuraj.cycle.fill.models.ItemCollection
import com.muthuraj.cycle.fill.util.ViewEvent

sealed interface ItemDetailScreenEvent : ViewEvent {
    data object AddCollectionClicked : ItemDetailScreenEvent
    data class AddCollection(val name: String) : ItemDetailScreenEvent
    data class ShowDeleteConfirmation(val collection: ItemCollection) : ItemDetailScreenEvent
    data object ConfirmDelete : ItemDetailScreenEvent
    data object DismissDeleteConfirmation : ItemDetailScreenEvent
    data class CollectionClicked(val collection: ItemCollection) : ItemDetailScreenEvent
    data object DismissDialog : ItemDetailScreenEvent
    data object Retry : ItemDetailScreenEvent
} 