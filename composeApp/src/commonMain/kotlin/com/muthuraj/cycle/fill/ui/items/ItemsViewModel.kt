package com.muthuraj.cycle.fill.ui.items

import androidx.lifecycle.viewModelScope
import com.muthuraj.cycle.fill.navigation.Screen
import com.muthuraj.cycle.fill.network.ItemResponse
import com.muthuraj.cycle.fill.network.NetworkManager
import com.muthuraj.cycle.fill.util.BaseViewModel
import com.muthuraj.cycle.fill.util.getDaysElapsed
import com.muthuraj.cycle.fill.util.getDaysElapsedUntil
import com.muthuraj.cycle.fill.util.log
import com.muthuraj.cycle.fill.util.printDebugStackTrace
import com.muthuraj.cycle.fill.util.toDateWithDayName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Inject
class ItemsViewModel(
    private val networkManager: NetworkManager,
    @Assisted private val items: Screen.Items
) : BaseViewModel<ItemsScreenEvent, ItemsScreenState>() {

    override fun setInitialState(): ItemsScreenState =
        ItemsScreenState.Loading

    private val collectionName = items.collectionName

    private val searchTextFlow = MutableStateFlow("")

    private var rawItems: List<ItemResponse> = emptyList()

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
                    val data = response.data!!
                    rawItems = data
                    searchTextFlow.debounce(300).collectLatest { searchText ->
                        withContext(Dispatchers.Default) {
                            val dates = if (searchText.isNotEmpty()) {
                                data.filter {
                                    it.date.contains(searchText, ignoreCase = true) ||
                                            it.description.contains(searchText, ignoreCase = true)
                                }
                            } else {
                                data
                            }.mapIndexed { index, item ->
                                val (date, weekDay) = item.date.toDateWithDayName()
                                val previousTimeStamp = data.getOrNull(index + 1)
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
                        }
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

            is ItemsScreenEvent.Search -> {
                searchTextFlow.value = event.searchText
            }

            ItemsScreenEvent.ShowExport -> {
                val selectedIds = (viewState.value as? ItemsScreenState.Success)
                    ?.selectedItemIds
                    ?.takeIf { it.isNotEmpty() }
                val json = buildExportJson(selectedIds)
                setState {
                    (this as? ItemsScreenState.Success)?.copy(exportJson = json)
                        ?: this
                }
            }

            is ItemsScreenEvent.EnterSelectionMode -> {
                setState {
                    (this as? ItemsScreenState.Success)?.copy(
                        selectionAnchorId = event.itemId,
                        selectedItemIds = setOf(event.itemId)
                    ) ?: this
                }
            }

            is ItemsScreenEvent.ToggleSelection -> {
                setState {
                    val state = this as? ItemsScreenState.Success ?: return@setState this
                    val updated = if (event.itemId in state.selectedItemIds) {
                        state.selectedItemIds - event.itemId
                    } else {
                        state.selectedItemIds + event.itemId
                    }
                    if (updated.isEmpty()) {
                        state.copy(selectedItemIds = emptySet(), selectionAnchorId = null)
                    } else {
                        state.copy(selectedItemIds = updated)
                    }
                }
            }

            ItemsScreenEvent.SelectUpToTop -> {
                setState {
                    val state = this as? ItemsScreenState.Success ?: return@setState this
                    val anchorIndex =
                        state.dates.indexOfFirst { it.id == state.selectionAnchorId }
                    if (anchorIndex == -1) return@setState this
                    state.copy(
                        selectedItemIds = state.selectedItemIds +
                                state.dates.take(anchorIndex + 1).map { it.id }
                    )
                }
            }

            ItemsScreenEvent.SelectUpToBottom -> {
                setState {
                    val state = this as? ItemsScreenState.Success ?: return@setState this
                    val anchorIndex =
                        state.dates.indexOfFirst { it.id == state.selectionAnchorId }
                    if (anchorIndex == -1) return@setState this
                    state.copy(
                        selectedItemIds = state.selectedItemIds +
                                state.dates.drop(anchorIndex).map { it.id }
                    )
                }
            }

            ItemsScreenEvent.ExitSelectionMode -> {
                setState {
                    (this as? ItemsScreenState.Success)?.copy(
                        selectedItemIds = emptySet(),
                        selectionAnchorId = null
                    ) ?: this
                }
            }

            ItemsScreenEvent.DismissExport -> {
                setState {
                    (this as? ItemsScreenState.Success)?.copy(exportJson = null)
                        ?: this
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun buildExportJson(selectedIds: Set<Int>? = null): String {
        val data = if (selectedIds != null) {
            rawItems.filter { it.id in selectedIds }
        } else {
            rawItems
        }
        val intervals = data.mapIndexedNotNull { index, item ->
            data.getOrNull(index + 1)?.date?.getDaysElapsedUntil(item.date)?.first
        }
        val jsonObject = buildJsonObject {
            put("collection", collectionName)
            put(
                "exportedOn",
                Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                    .toString()
            )
            put("totalEntries", data.size)
            if (data.size < rawItems.size) {
                put("partialExport", true)
            }
            if (data.isNotEmpty()) {
                putJsonObject("stats") {
                    put("daysSinceLastEntry", data.first().date.getDaysElapsed().first)
                    if (intervals.isNotEmpty()) {
                        put(
                            "averageIntervalDays",
                            (intervals.average() * 10).roundToInt() / 10.0
                        )
                        put("minIntervalDays", intervals.min())
                        put("maxIntervalDays", intervals.max())
                    }
                }
            }
            putJsonArray("entries") {
                data.forEachIndexed { index, item ->
                    addJsonObject {
                        put("date", item.date)
                        put("weekDay", item.date.toDateWithDayName().second)
                        if (item.description.isNotBlank()) {
                            put("note", item.description)
                        }
                        data.getOrNull(index + 1)?.let { previous ->
                            put(
                                "daysSincePrevious",
                                previous.date.getDaysElapsedUntil(item.date).first
                            )
                        }
                    }
                }
            }
        }
        return prettyJson.encodeToString(JsonObject.serializer(), jsonObject)
    }

    companion object {
        private val prettyJson = Json { prettyPrint = true }
    }
}