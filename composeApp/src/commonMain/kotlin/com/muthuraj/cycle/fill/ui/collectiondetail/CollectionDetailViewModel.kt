package com.muthuraj.cycle.fill.ui.collectiondetail

import androidx.lifecycle.viewModelScope
import com.muthuraj.cycle.fill.navigation.NavigationManager
import com.muthuraj.cycle.fill.navigation.Screen
import com.muthuraj.cycle.fill.util.BaseViewModel
import com.muthuraj.cycle.fill.util.daysAgoFrom
import com.muthuraj.cycle.fill.util.getDaysElapsedUntil
import com.muthuraj.cycle.fill.util.log
import com.muthuraj.cycle.fill.util.printDebugStackTrace
import com.muthuraj.cycle.fill.util.toDateWithDayName
import com.muthuraj.cycle.fill.util.toStringKey
import com.muthuraj.cycle.fill.util.toTimestamp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class CollectionDetailViewModel(
    private val navigationManager: NavigationManager,
    @Assisted private val collectionDetail: Screen.CollectionDetail
) : BaseViewModel<CollectionDetailScreenEvent, CollectionDetailScreenState>() {

    override fun setInitialState(): CollectionDetailScreenState =
        CollectionDetailScreenState.Loading

    private val documentPath = collectionDetail.documentPath
    private val collectionName = collectionDetail.collectionName
    private val collectionCommentName = "${collectionDetail.collectionName}_comments"
    private var timeStampList = listOf<Timestamp>()
    private var comments = mapOf<String, String>()

    init {
        loadDates()
    }

    private var job: Job? = null
    private fun loadDates() {
        job?.cancel()
        job = Firebase.firestore.document(documentPath)
            .snapshots
            .onEach { documentSnapshot ->
                timeStampList = documentSnapshot.get<List<Timestamp>>(collectionName) {
                    serializersModule = SerializersModule {
                        contextual(ListSerializer(Timestamp.serializer()))
                    }
                }
                comments =
                    documentSnapshot.get<Map<String, String>?>(collectionCommentName) {
                        serializersModule = SerializersModule {
                            contextual(
                                MapSerializer(
                                    String.serializer(),
                                    String.serializer()
                                )
                            )
                        }
                    }.orEmpty()
                val timeStampComments = comments.mapKeys { it.key.toTimestamp() }
                val sortedDate = timeStampList.sortedByDescending { it.seconds }
                val dates = sortedDate.mapIndexed { index, timestamp ->
                    val (date, weekDay) = timestamp.toDateWithDayName()
                    val previousTimeStamp = sortedDate.getOrNull(index + 1)
                    val daysAgoForLastCycle = previousTimeStamp?.getDaysElapsedUntil(timestamp)
                    val comment = timeStampComments[timestamp]

                    CollectionDetailItem(
                        date = date,
                        daysAgoForLastCycle = daysAgoForLastCycle,
                        weekDay = weekDay,
                        timestamp = timestamp,
                        comment = comment
                    )
                }

                setState {
                    CollectionDetailScreenState.Success(
                        collectionName = collectionName,
                        dates = dates
                    )
                }
            }
            .catch { error ->
                log { "Error loading dates: $error" }
                setState {
                    CollectionDetailScreenState.Error("Failed to load dates")
                }
            }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
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
                        val instant = event.dateTime.toInstant(TimeZone.currentSystemDefault())
                        val timestamp = Timestamp(instant.epochSeconds, instant.nanosecondsOfSecond)

                        val currentDates = timeStampList
                        Firebase.firestore.document(documentPath)
                            .update(mapOf(collectionName to currentDates + timestamp))

                        setState {
                            (this as? CollectionDetailScreenState.Success)?.copy(showAddDialog = false)
                                ?: this
                        }
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
                        deleteConfirmation = event.timestamp
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
                val timestamp =
                    (currentState as? CollectionDetailScreenState.Success)?.deleteConfirmation
                if (timestamp != null) {
                    viewModelScope.launch {
                        try {
                            val updatedList = timeStampList.filter { it != timestamp }
                            Firebase.firestore.document(documentPath)
                                .update(mapOf(collectionName to updatedList))

                            val map = comments.toMutableMap().apply {
                                remove(timestamp.toStringKey())
                            }
                            Firebase.firestore.document(documentPath)
                                .update(mapOf(collectionCommentName to map))
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
                        val map = comments.toMutableMap().apply {
                            set(event.timestamp.toStringKey(), event.comment)
                        }
                        Firebase.firestore.document(documentPath)
                            .update(mapOf(collectionCommentName to map))
                    } catch (e: Exception) {
                        e.printDebugStackTrace()
                        log { "Error deleting date: $e" }
                    }
                }
            }
        }
    }
}