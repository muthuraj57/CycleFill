package com.muthuraj.cycle.fill.ui.collections

import androidx.lifecycle.viewModelScope
import com.muthuraj.cycle.fill.models.Collection
import com.muthuraj.cycle.fill.navigation.NavigationManager
import com.muthuraj.cycle.fill.navigation.Screen
import com.muthuraj.cycle.fill.network.NetworkManager
import com.muthuraj.cycle.fill.util.BaseViewModel
import com.muthuraj.cycle.fill.util.formatToIndianRupee
import com.muthuraj.cycle.fill.util.getDaysElapsed
import com.muthuraj.cycle.fill.util.log
import com.muthuraj.cycle.fill.util.printDebugStackTrace
import com.muthuraj.cycle.fill.util.toDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class CollectionsViewModel(
    private val navigationManager: NavigationManager,
    private val networkManager: NetworkManager,
    @Assisted private val collections: Screen.Collections
) : BaseViewModel<CollectionsScreenEvent, CollectionsScreenState>() {

    override fun setInitialState(): CollectionsScreenState = CollectionsScreenState.Loading

    init {
        loadItemDetails()
    }

    private var job: Job? = null
    private fun loadItemDetails() {
        job?.cancel()
        job = viewModelScope.launch {
            val result = runCatching {
                networkManager.getCollections(subCategoryId = collections.subCategoryId)
            }
            if (result.isSuccess) {
                val response = result.getOrThrow()
                if (response.success) {
                    val collections = response.data!!
                        .map {
                            Collection(
                                id = it.id,
                                name = it.name,
                                documentPath = it.id.toString(),
                                lastRefillDate = it.lastDate?.toDate(),
                                daysElapsed = it.lastDate?.getDaysElapsed(),
                                totalAmount = it.totalAmount?.let(::formatToIndianRupee)
                            )
                        }
                    setState {
                        CollectionsScreenState.Success(
                            itemName = this@CollectionsViewModel.collections.itemName,
                            collections = collections
                        )
                    }
                } else {
                    log { "Error loading item details: ${response.message}" }
                    setState {
                        CollectionsScreenState.Error(response.message!!)
                    }
                }
            } else {
                val error = result.exceptionOrNull()!!
                error.printDebugStackTrace()
                log { "Error loading item details: $error" }
                setState {
                    CollectionsScreenState.Error(error.message ?: "Failed to load item details")

                }
            }
        }
    }

    override fun handleEvents(event: CollectionsScreenEvent) {
        when (event) {
            is CollectionsScreenEvent.CollectionClicked -> {
                viewModelScope.launch {
                    navigationManager.navigate(
                        Screen.Items(
                            collectionId = event.collection.id,
                            collectionName = event.collection.name
                        )
                    )
                }
            }

            CollectionsScreenEvent.AddCollectionClicked -> {
                setState {
                    (this as? CollectionsScreenState.Success)?.copy(showAddDialog = true)
                        ?: this
                }
            }

            is CollectionsScreenEvent.AddCollection -> {
                viewModelScope.launch {
                    try {
                        networkManager.addCollection(
                            subCategoryId = collections.subCategoryId,
                            name = event.name
                        )
                        loadItemDetails()
                    } catch (e: Exception) {
                        log { "Error adding collection: $e" }
                        // Optionally show error message to user
                    }
                }
            }

            CollectionsScreenEvent.DismissDialog -> {
                setState {
                    (this as? CollectionsScreenState.Success)?.copy(showAddDialog = false)
                        ?: this
                }
            }

            CollectionsScreenEvent.Retry -> {
                setState { CollectionsScreenState.Loading }
                loadItemDetails()
            }

            is CollectionsScreenEvent.ShowDeleteConfirmation -> {
                setState {
                    (this as? CollectionsScreenState.Success)?.copy(
                        deleteConfirmation = event.collection
                    ) ?: this
                }
            }

            CollectionsScreenEvent.DismissDeleteConfirmation -> {
                setState {
                    (this as? CollectionsScreenState.Success)?.copy(
                        deleteConfirmation = null
                    ) ?: this
                }
            }

            CollectionsScreenEvent.ConfirmDelete -> {
                val currentState = viewState.value
                val collection =
                    (currentState as? CollectionsScreenState.Success)?.deleteConfirmation
                if (collection != null) {
                    viewModelScope.launch {
                        try {
                            networkManager.deleteCollection(collectionId = collection.id)
                            loadItemDetails()
                        } catch (e: Exception) {
                            log { "Error deleting collection: $e" }
                        }
                    }
                }
                setState {
                    (this as? CollectionsScreenState.Success)?.copy(
                        deleteConfirmation = null
                    ) ?: this
                }
            }
        }
    }
}