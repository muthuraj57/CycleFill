/* $Id$ */
package com.muthuraj.cycle.fill.ui.dashboard

import androidx.lifecycle.viewModelScope
import com.muthuraj.cycle.fill.models.Category
import com.muthuraj.cycle.fill.models.firebase.FirebaseCategory
import com.muthuraj.cycle.fill.navigation.NavigationManager
import com.muthuraj.cycle.fill.navigation.Screen
import com.muthuraj.cycle.fill.util.BaseViewModel
import com.muthuraj.cycle.fill.util.firebase.firebaseStorage
import com.muthuraj.cycle.fill.util.log
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import me.tatarka.inject.annotations.Inject

/**
 * Created by Muthuraj on 07/12/24.
 */
@Inject
class DashboardScreenViewModel(private val navigationManager: NavigationManager) :
    BaseViewModel<DashboardScreenEvent, DashboardScreenState>() {
    override fun setInitialState(): DashboardScreenState {
        return DashboardScreenState.Loading
    }

    init {
        loadData()
    }

    private var job: Job? = null
    private fun loadData() {
        setState { DashboardScreenState.Loading }
        job?.cancel()
        job = Firebase.firestore
            .collection("categories")
            .snapshots
            .onEach { querySnapshot ->
                val categories = querySnapshot.documents.map { documentSnapshot ->

                    val test = documentSnapshot.data<FirebaseCategory> {
                        serializersModule = SerializersModule {
                            contextual(FirebaseCategory.serializer())
                        }
                    }
                    val imageUrl = firebaseStorage.reference(test.imagePath).getDownloadUrl()
                    Category(
                        name = test.name,
                        imageUrl = imageUrl,
                        documentPath = documentSnapshot.reference.path
                    )
                }
                setState { DashboardScreenState.Success(categories) }

            }
            .catch { error ->
                log { "Error loading item details: $error" }
                setState {
                    DashboardScreenState.Error("Failed to load categories")
                }
            }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    override fun handleEvents(event: DashboardScreenEvent) {
        when (event) {
            is DashboardScreenEvent.CategoryClicked -> {
                viewModelScope.launch {
                    navigationManager.navigate(
                        Screen.ItemDetail(
                            documentPath = event.category.documentPath,
                            itemName = event.category.name
                        )
                    )
                }
            }

            DashboardScreenEvent.Retry -> loadData()
        }
    }
}