package com.muthuraj.cycle.fill.ui.itemdetail

import com.muthuraj.cycle.fill.models.ItemCollection
import com.muthuraj.cycle.fill.util.ViewState

sealed interface ItemDetailScreenState : ViewState {
    data object Loading : ItemDetailScreenState
    data class Error(val message: String) : ItemDetailScreenState
    data class Success(
        val itemName: String,
        val collections: List<ItemCollection>,
        val showAddDialog: Boolean = false,
        val deleteConfirmation: ItemCollection? = null
    ) : ItemDetailScreenState
} 