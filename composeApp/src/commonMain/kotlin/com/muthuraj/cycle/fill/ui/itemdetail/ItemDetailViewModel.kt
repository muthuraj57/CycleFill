package com.muthuraj.cycle.fill.ui.itemdetail

import androidx.lifecycle.viewModelScope
import com.muthuraj.cycle.fill.models.ItemCollection
import com.muthuraj.cycle.fill.navigation.NavigationManager
import com.muthuraj.cycle.fill.navigation.Screen
import com.muthuraj.cycle.fill.network.NetworkManager
import com.muthuraj.cycle.fill.util.BaseViewModel
import com.muthuraj.cycle.fill.util.getDaysElapsed
import com.muthuraj.cycle.fill.util.log
import com.muthuraj.cycle.fill.util.printDebugStackTrace
import com.muthuraj.cycle.fill.util.toDate
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.toDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapEntrySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ItemDetailViewModel(
    private val navigationManager: NavigationManager,
    private val networkManager: NetworkManager,
    @Assisted private val itemDetail: Screen.ItemDetail
) : BaseViewModel<ItemDetailScreenEvent, ItemDetailScreenState>() {

    override fun setInitialState(): ItemDetailScreenState = ItemDetailScreenState.Loading

    init {
        loadItemDetails()
    }

    private var job: Job? = null
    private fun loadItemDetails() {
        job?.cancel()
        job = viewModelScope.launch {
            val result = runCatching {
                networkManager.getCollections(subCategoryId = itemDetail.subCategoryId)
            }
            if (result.isSuccess) {
                val response = result.getOrThrow()
                if (response.success) {
                    val collections = response.data!!
                        .map {
                            ItemCollection(
                                id = it.id,
                                name = it.name,
                                documentPath = it.id.toString(),
                                lastRefillDate = it.lastDate?.toDate(),
                                daysElapsed = it.lastDate?.getDaysElapsed()
                            )
                        }
                    setState {
                        ItemDetailScreenState.Success(
                            itemName = itemDetail.itemName,
                            collections = collections
                        )
                    }
                } else {
                    log { "Error loading item details: ${response.message}" }
                    setState {
                        ItemDetailScreenState.Error(response.message!!)
                    }
                }
            } else {
                val error = result.exceptionOrNull()!!
                error.printDebugStackTrace()
                log { "Error loading item details: $error" }
                setState {
                    ItemDetailScreenState.Error(error.message ?: "Failed to load item details")

                }
            }
        }
    }

    override fun handleEvents(event: ItemDetailScreenEvent) {
        when (event) {
            is ItemDetailScreenEvent.CollectionClicked -> {
                viewModelScope.launch {
                    navigationManager.navigate(
                        Screen.CollectionDetail(
                            collectionId = event.collection.id,
                            collectionName = event.collection.name
                        )
                    )
                }
            }

            ItemDetailScreenEvent.AddCollectionClicked -> {
                setState {
                    (this as? ItemDetailScreenState.Success)?.copy(showAddDialog = true)
                        ?: this
                }
            }

            is ItemDetailScreenEvent.AddCollection -> {
                viewModelScope.launch {
                    try {
                        networkManager.addCollection(
                            subCategoryId = itemDetail.subCategoryId,
                            name = event.name
                        )
                        loadItemDetails()
                    } catch (e: Exception) {
                        log { "Error adding collection: $e" }
                        // Optionally show error message to user
                    }
                }
            }

            ItemDetailScreenEvent.DismissDialog -> {
                setState {
                    (this as? ItemDetailScreenState.Success)?.copy(showAddDialog = false)
                        ?: this
                }
            }

            ItemDetailScreenEvent.Retry -> {
                setState { ItemDetailScreenState.Loading }
                loadItemDetails()
            }

            is ItemDetailScreenEvent.ShowDeleteConfirmation -> {
                setState {
                    (this as? ItemDetailScreenState.Success)?.copy(
                        deleteConfirmation = event.collection
                    ) ?: this
                }
            }

            ItemDetailScreenEvent.DismissDeleteConfirmation -> {
                setState {
                    (this as? ItemDetailScreenState.Success)?.copy(
                        deleteConfirmation = null
                    ) ?: this
                }
            }

            ItemDetailScreenEvent.ConfirmDelete -> {
                val currentState = viewState.value
                val collection =
                    (currentState as? ItemDetailScreenState.Success)?.deleteConfirmation
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
                    (this as? ItemDetailScreenState.Success)?.copy(
                        deleteConfirmation = null
                    ) ?: this
                }
            }
        }
    }
}