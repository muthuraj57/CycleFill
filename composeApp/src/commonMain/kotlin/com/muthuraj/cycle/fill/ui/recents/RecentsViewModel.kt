package com.muthuraj.cycle.fill.ui.recents

import androidx.lifecycle.viewModelScope
import com.muthuraj.cycle.fill.di.AppScope
import com.muthuraj.cycle.fill.network.ItemDetailedResponse
import com.muthuraj.cycle.fill.network.NetworkManager
import com.muthuraj.cycle.fill.util.BaseViewModel
import com.muthuraj.cycle.fill.util.combine
import com.muthuraj.cycle.fill.util.getDaysElapsedUntil
import com.muthuraj.cycle.fill.util.log
import com.muthuraj.cycle.fill.util.printDebugStackTrace
import com.muthuraj.cycle.fill.util.toDateWithDayName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@AppScope
@Inject
class RecentsViewModel(
    private val networkManager: NetworkManager
) : BaseViewModel<RecentsScreenEvent, RecentsScreenState>() {

    private val isPrivacyEnabled = MutableStateFlow(false)
    val isPrivacyEnabledFlow: StateFlow<Boolean> = isPrivacyEnabled

    private val searchTextFlow = MutableStateFlow("")

    override fun setInitialState(): RecentsScreenState = RecentsScreenState.Loading

    init {
        log { "init() called" }
    }

    private var job: Job? = null

    private fun loadDates() {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.Default) {
            val result = runCatching {
                networkManager.getAllItems()
            }
            if (result.isSuccess) {
                val response = result.getOrThrow()
                if (response.success) {
                    isPrivacyEnabled.combine(searchTextFlow.debounce(300)) { isPrivacyEnabled, searchText ->
                        isPrivacyEnabled to searchText
                    }.collectLatest { (isPrivacyEnabled, searchText) ->
                        withContext(Dispatchers.Default) {
                            val data = if (isPrivacyEnabled || searchText.isNotEmpty()) {
                                response.data!!
                                    .filter { item ->
                                        filterItem(
                                            isPrivacyEnabled = isPrivacyEnabled,
                                            item = item,
                                            searchText = searchText
                                        )
                                    }
                            } else {
                                response.data!!
                            }
                            val dates = data.mapIndexed { index, item ->
                                val (date, weekDay) = item.date.toDateWithDayName()
                                val previousTimeStamp = response.data.getOrNull(index + 1)
                                val daysAgoForLastCycle =
                                    previousTimeStamp?.date?.getDaysElapsedUntil(item.date)

                                // Get previous item to compare headers
                                val previousItem = if (index > 0) response.data[index - 1] else null

                                ItemDetailed(
                                    id = item.id,
                                    date = date,
                                    daysAgoForLastCycle = daysAgoForLastCycle,
                                    weekDay = weekDay,
                                    timestamp = item.date,
                                    comment = item.description,
                                    categoryName = item.category_name,
                                    subCategoryName = item.subcategory_name,
                                    collectionName = item.collection_name,
                                    // Only show headers if they're different from previous item
                                    showCategoryName = previousItem?.category_name != item.category_name,
                                    showSubCategoryName = previousItem?.subcategory_name != item.subcategory_name,
                                    showCollectionName = previousItem?.collection_name != item.collection_name,
                                    number = "${data.size - index}"
                                )
                            }
                            val recentData = groupItemsToRecentData(dates)
                            setState {
                                RecentsScreenState.Success(
                                    dates = recentData,
                                    isPrivacyEnabled = isPrivacyEnabled
                                )
                            }
                        }
                    }
                } else {
                    log { "Error loading recent items: ${response.message}" }
                    setState {
                        RecentsScreenState.Error(response.message!!)
                    }
                }
            } else {
                val error = result.exceptionOrNull()!!
                error.printDebugStackTrace()
                log { "Error loading recent items: $error" }
                setState {
                    RecentsScreenState.Error(
                        error.message ?: "Failed to load recent items"
                    )
                }
            }
        }
    }

    private fun filterItem(
        isPrivacyEnabled: Boolean,
        item: ItemDetailedResponse,
        searchText: String
    ): Boolean {
        val privateFilter = if (isPrivacyEnabled) {
            item.category_name != "Private"
        } else {
            true
        }
        return privateFilter && (
                item.category_name.contains(
                    searchText,
                    ignoreCase = true
                ) ||
                        item.subcategory_name.contains(
                            searchText,
                            ignoreCase = true
                        ) ||
                        item.collection_name.contains(
                            searchText,
                            ignoreCase = true
                        ) ||
                        item.description.contains(
                            searchText,
                            ignoreCase = true
                        ) ||
                        item.date.contains(
                            searchText,
                            ignoreCase = true
                        )
                )
    }

    private fun groupItemsToRecentData(items: List<ItemDetailed>): RecentData {
        val recentCategories = mutableListOf<RecentCategory>()

        items.forEach { item ->
            // Find or create the appropriate category
            var category = recentCategories.lastOrNull()?.takeIf { it.name == item.categoryName }
            if (category == null) {
                category = RecentCategory(item.categoryName, mutableListOf())
                recentCategories.add(category)
            }

            // Find or create the appropriate subcategory within the category
            var subCategory =
                category.subCategories.lastOrNull()?.takeIf { it.name == item.subCategoryName }
            if (subCategory == null) {
                subCategory = RecentSubCategory(item.subCategoryName, mutableListOf())
                category.subCategories.add(subCategory)
            }

            // Find or create the appropriate collection within the subcategory
            var collection =
                subCategory.collections.lastOrNull()?.takeIf { it.name == item.collectionName }
            if (collection == null) {
                collection = RecentCollection(item.collectionName, mutableListOf())
                subCategory.collections.add(collection)
            }

            // Add the item to the collection
            collection.items.add(item)
        }

        return RecentData(recentCategories)
    }


    override fun handleEvents(event: RecentsScreenEvent) {
        when (event) {
            RecentsScreenEvent.Retry -> {
                setState { RecentsScreenState.Loading }
                loadDates()
            }

            RecentsScreenEvent.ScreenOpened -> {
                searchTextFlow.value = ""
                loadDates()
            }

            RecentsScreenEvent.PrivacyViewClicked -> {
                isPrivacyEnabled.update { !it }
            }

            is RecentsScreenEvent.Search -> {
                searchTextFlow.value = event.searchText
            }
        }
    }
} 