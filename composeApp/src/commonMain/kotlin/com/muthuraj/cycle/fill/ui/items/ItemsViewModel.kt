package com.muthuraj.cycle.fill.ui.items

import androidx.lifecycle.viewModelScope
import com.muthuraj.cycle.fill.navigation.Screen
import com.muthuraj.cycle.fill.network.NetworkManager
import com.muthuraj.cycle.fill.util.BaseViewModel
import com.muthuraj.cycle.fill.util.getDaysElapsedUntil
import com.muthuraj.cycle.fill.util.log
import com.muthuraj.cycle.fill.util.printDebugStackTrace
import com.muthuraj.cycle.fill.util.toDateWithDayName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ItemsViewModel(
    private val networkManager: NetworkManager,
    @Assisted private val items: Screen.Items
) : BaseViewModel<ItemsScreenEvent, ItemsScreenState>() {

    override fun setInitialState(): ItemsScreenState =
        ItemsScreenState.Loading

    private val collectionName = items.collectionName

    init {
        loadDates()
    }

    private var job: Job? = null
    private fun loadDates() {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.Default) {
            val result = runCatching {
                networkManager.getItems(collectionId = items.collectionId)
            }
            if (result.isSuccess) {
                val response = result.getOrThrow()
                if (response.success) {
                    val dates = response.data!!.mapIndexed { index, item ->
                        val (date, weekDay) = item.date.toDateWithDayName()
                        val previousTimeStamp = response.data.getOrNull(index + 1)
                        val daysAgoForLastCycle =
                            previousTimeStamp?.date?.getDaysElapsedUntil(item.date)
                        Item(
                            id = item.id,
                            date = date,
                            daysAgoForLastCycle = daysAgoForLastCycle,
                            weekDay = weekDay,
                            timestamp = item.date,
                            comment = item.description
                        )
                    }
                    setState {
                        ItemsScreenState.Success(
                            collectionName = collectionName,
                            dates = dates
                        )
                    }
                } else {
                    log { "Error loading collection items: ${response.message}" }
                    setState {
                        ItemsScreenState.Error(response.message!!)
                    }
                }
            } else {
                val error = result.exceptionOrNull()!!
                error.printDebugStackTrace()
                log { "Error loading collection items: $error" }
                setState {
                    ItemsScreenState.Error(
                        error.message ?: "Failed to load collection items"
                    )

                }
            }
        }
    }

    override fun handleEvents(event: ItemsScreenEvent) {
        when (event) {
            ItemsScreenEvent.AddDateClicked -> {
                setState {
                    (this as? ItemsScreenState.Success)?.copy(showAddDialog = true)
                        ?: this
                }
            }

            is ItemsScreenEvent.AddDate -> {
                viewModelScope.launch {
                    try {
                        networkManager.addItem(
                            date = event.date,
                            collectionId = items.collectionId,
                            description = ""
                        )
                        loadDates()
                    } catch (e: Exception) {
                        e.printDebugStackTrace()
                        log { "Error adding date: $e" }
                    }
                }
            }

            ItemsScreenEvent.DismissDialog -> {
                setState {
                    (this as? ItemsScreenState.Success)?.copy(showAddDialog = false)
                        ?: this
                }
            }

            ItemsScreenEvent.Retry -> {
                setState { ItemsScreenState.Loading }
                loadDates()
            }

            is ItemsScreenEvent.ShowDeleteConfirmation -> {
                setState {
                    (this as? ItemsScreenState.Success)?.copy(
                        deleteConfirmation = event.itemId
                    ) ?: this
                }
            }

            ItemsScreenEvent.DismissDeleteConfirmation -> {
                setState {
                    (this as? ItemsScreenState.Success)?.copy(
                        deleteConfirmation = null
                    ) ?: this
                }
            }

            ItemsScreenEvent.ConfirmDelete -> {
                val currentState = viewState.value
                val id =
                    (currentState as? ItemsScreenState.Success)?.deleteConfirmation
                if (id != null) {
                    viewModelScope.launch {
                        try {
                            networkManager.deleteItem(itemId = id)
                            loadDates()
                        } catch (e: Exception) {
                            e.printDebugStackTrace()
                            log { "Error deleting date: $e" }
                        }
                    }
                }
                setState {
                    (this as? ItemsScreenState.Success)?.copy(
                        deleteConfirmation = null
                    ) ?: this
                }
            }

            is ItemsScreenEvent.AddComment -> {
                viewModelScope.launch {
                    try {
                        networkManager.updateItemDescription(
                            itemId = event.itemId,
                            description = event.comment
                        )
                        loadDates()
                    } catch (e: Exception) {
                        e.printDebugStackTrace()
                        log { "Error deleting date: $e" }
                    }
                }
            }
        }
    }
}