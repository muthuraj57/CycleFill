package com.muthuraj.cycle.fill.ui.recents

import androidx.lifecycle.viewModelScope
import com.muthuraj.cycle.fill.network.NetworkManager
import com.muthuraj.cycle.fill.ui.items.Item
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
                        RecentsScreenState.Success(dates = dates)
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

    override fun handleEvents(event: RecentsScreenEvent) {
        when (event) {
            RecentsScreenEvent.Retry -> {
                setState { RecentsScreenState.Loading }
                loadDates()
            }
        }
    }
} 