package com.muthuraj.cycle.fill.ui.recents

import com.muthuraj.cycle.fill.util.ViewEvent

sealed interface RecentsScreenEvent : ViewEvent {
    data object Retry : RecentsScreenEvent
    data object ScreenOpened : RecentsScreenEvent
    data object PrivacyViewClicked : RecentsScreenEvent
    data class Search(val searchText: String) : RecentsScreenEvent
} 