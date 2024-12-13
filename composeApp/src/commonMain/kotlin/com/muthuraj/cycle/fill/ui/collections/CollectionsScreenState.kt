package com.muthuraj.cycle.fill.ui.collections

import com.muthuraj.cycle.fill.models.Collection
import com.muthuraj.cycle.fill.util.ViewState

sealed interface CollectionsScreenState : ViewState {
    data object Loading : CollectionsScreenState
    data class Error(val message: String) : CollectionsScreenState
    data class Success(
        val itemName: String,
        val collections: List<Collection>,
        val showAddDialog: Boolean = false,
        val deleteConfirmation: Collection? = null
    ) : CollectionsScreenState
} 