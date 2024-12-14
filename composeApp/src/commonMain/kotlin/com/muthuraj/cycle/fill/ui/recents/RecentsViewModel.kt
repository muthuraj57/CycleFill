package com.muthuraj.cycle.fill.ui.recents

import androidx.lifecycle.viewModelScope
import com.muthuraj.cycle.fill.network.NetworkManager
import com.muthuraj.cycle.fill.util.BaseViewModel
import com.muthuraj.cycle.fill.util.getDaysElapsedUntil
import com.muthuraj.cycle.fill.util.log
import com.muthuraj.cycle.fill.util.printDebugStackTrace
import com.muthuraj.cycle.fill.util.toDateWithDayName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class RecentsViewModel(
    private val networkManager: NetworkManager
) : BaseViewModel<RecentsScreenEvent, RecentsScreenState>() {

    override fun setInitialState(): RecentsScreenState = RecentsScreenState.Loading

    init {
        loadDates()
    }

    private fun loadDates() {
        viewModelScope.launch(Dispatchers.Default) {
            val result = runCatching {
                networkManager.getAllItems()
            }
            if (result.isSuccess) {
                val response = result.getOrThrow()
                if (response.success) {
                    val dates = response.data!!.mapIndexed { index, item ->
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
                            number = "${response.data.size - index}"
                        )
                    }

                    val recentData = groupItemsToRecentData(dates)
                    setState {
                        RecentsScreenState.Success(dates = recentData)
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
        }
    }
} 