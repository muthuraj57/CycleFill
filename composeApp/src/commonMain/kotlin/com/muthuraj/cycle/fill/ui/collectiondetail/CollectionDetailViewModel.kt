package com.muthuraj.cycle.fill.ui.collectiondetail

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
class CollectionDetailViewModel(
    private val networkManager: NetworkManager,
    @Assisted private val collectionDetail: Screen.CollectionDetail
) : BaseViewModel<CollectionDetailScreenEvent, CollectionDetailScreenState>() {

    override fun setInitialState(): CollectionDetailScreenState =
        CollectionDetailScreenState.Loading

    private val collectionName = collectionDetail.collectionName

    init {
        loadDates()
    }

    private var job: Job? = null
    private fun loadDates() {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.Default) {
            val result = runCatching {
                networkManager.getItems(collectionId = collectionDetail.collectionId)
            }
            if (result.isSuccess) {
                val response = result.getOrThrow()
                if (response.success) {
                    val dates = response.data!!.mapIndexed { index, item ->
                        val (date, weekDay) = item.date.toDateWithDayName()
                        val previousTimeStamp = response.data.getOrNull(index + 1)
                        val daysAgoForLastCycle =
                            previousTimeStamp?.date?.getDaysElapsedUntil(item.date)
                        CollectionDetailItem(
                            id = item.id,
                            date = date,
                            daysAgoForLastCycle = daysAgoForLastCycle,
                            weekDay = weekDay,
                            timestamp = item.date,
                            comment = item.description
                        )
                    }
                    setState {
                        CollectionDetailScreenState.Success(
                            collectionName = collectionName,
                            dates = dates
                        )
                    }
                } else {
                    log { "Error loading collection items: ${response.message}" }
                    setState {
                        CollectionDetailScreenState.Error(response.message!!)
                    }
                }
            } else {
                val error = result.exceptionOrNull()!!
                error.printDebugStackTrace()
                log { "Error loading collection items: $error" }
                setState {
                    CollectionDetailScreenState.Error(
                        error.message ?: "Failed to load collection items"
                    )

                }
            }
        }
    }

    override fun handleEvents(event: CollectionDetailScreenEvent) {
        when (event) {
            CollectionDetailScreenEvent.AddDateClicked -> {
                setState {
                    (this as? CollectionDetailScreenState.Success)?.copy(showAddDialog = true)
                        ?: this
                }
            }

            is CollectionDetailScreenEvent.AddDate -> {
                viewModelScope.launch {
                    try {
                        networkManager.addItem(
                            date = event.date,
                            collectionId = collectionDetail.collectionId,
                            description = ""
                        )
                        loadDates()
                    } catch (e: Exception) {
                        e.printDebugStackTrace()
                        log { "Error adding date: $e" }
                    }
                }
            }

            CollectionDetailScreenEvent.DismissDialog -> {
                setState {
                    (this as? CollectionDetailScreenState.Success)?.copy(showAddDialog = false)
                        ?: this
                }
            }

            CollectionDetailScreenEvent.Retry -> {
                setState { CollectionDetailScreenState.Loading }
                loadDates()
            }

            is CollectionDetailScreenEvent.ShowDeleteConfirmation -> {
                setState {
                    (this as? CollectionDetailScreenState.Success)?.copy(
                        deleteConfirmation = event.itemId
                    ) ?: this
                }
            }

            CollectionDetailScreenEvent.DismissDeleteConfirmation -> {
                setState {
                    (this as? CollectionDetailScreenState.Success)?.copy(
                        deleteConfirmation = null
                    ) ?: this
                }
            }

            CollectionDetailScreenEvent.ConfirmDelete -> {
                val currentState = viewState.value
                val id =
                    (currentState as? CollectionDetailScreenState.Success)?.deleteConfirmation
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
                    (this as? CollectionDetailScreenState.Success)?.copy(
                        deleteConfirmation = null
                    ) ?: this
                }
            }

            is CollectionDetailScreenEvent.AddComment -> {
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