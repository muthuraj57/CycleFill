package com.muthuraj.cycle.fill.ui.itemdetail

import androidx.lifecycle.viewModelScope
import com.muthuraj.cycle.fill.models.ItemCollection
import com.muthuraj.cycle.fill.navigation.NavigationManager
import com.muthuraj.cycle.fill.navigation.Screen
import com.muthuraj.cycle.fill.util.BaseViewModel
import com.muthuraj.cycle.fill.util.getDaysElapsed
import com.muthuraj.cycle.fill.util.log
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
    @Assisted private val itemDetail: Screen.ItemDetail
) : BaseViewModel<ItemDetailScreenEvent, ItemDetailScreenState>() {

    override fun setInitialState(): ItemDetailScreenState = ItemDetailScreenState.Loading

    private val documentPath = itemDetail.documentPath

    init {
        loadItemDetails()
    }

    private var job: Job? = null
    private fun loadItemDetails() {
        val documentRef = Firebase.firestore.document(documentPath)
        job?.cancel()
        job = documentRef.snapshots
            .onEach { query ->
                val collection = query.data<Map<String, List<Timestamp>>> {
                    serializersModule = SerializersModule {
                        contextual(
                            MapEntrySerializer(
                                String.serializer(),
                                ListSerializer(Timestamp.serializer())
                            )
                        )
                    }
                }
                    .filter { it.key != "name" && it.key != "imagePath" && it.key.endsWith("_comments").not() }
                val collections = collection.map {
                    val lastTimeStamp = it.value
                        .maxByOrNull { it.toDuration() }
                    val lastDate = lastTimeStamp?.toDate()
                    ItemCollection(
                        name = it.key,
                        documentPath = documentPath,
                        lastRefillDate = lastDate,
                        daysElapsed = lastTimeStamp?.getDaysElapsed()
                    )
                }
                setState {
                    ItemDetailScreenState.Success(
                        itemName = itemDetail.itemName,
                        collections = collections
                    )
                }
            }.catch { error ->
                log { "Error loading item details: $error" }
                setState {
                    ItemDetailScreenState.Error("Failed to load item details")
                }
            }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    override fun handleEvents(event: ItemDetailScreenEvent) {
        when (event) {
            is ItemDetailScreenEvent.CollectionClicked -> {
                viewModelScope.launch {
                    navigationManager.navigate(
                        Screen.CollectionDetail(
                            documentPath = documentPath,
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
                        Firebase.firestore
                            .document(documentPath)
                            .update(mapOf(event.name to emptyList<String>()))

                        setState {
                            (this as? ItemDetailScreenState.Success)?.copy(showAddDialog = false)
                                ?: this
                        }
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
                            val updates = mapOf(collection.name to listOf<String>())
                            Firebase.firestore.document(documentPath)
                                .update(updates)
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