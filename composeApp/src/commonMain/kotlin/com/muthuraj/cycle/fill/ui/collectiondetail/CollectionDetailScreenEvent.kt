package com.muthuraj.cycle.fill.ui.collectiondetail

import com.muthuraj.cycle.fill.util.ViewEvent
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.LocalDateTime

sealed interface CollectionDetailScreenEvent : ViewEvent {
    data object AddDateClicked : CollectionDetailScreenEvent
    data class AddDate(val dateTime: LocalDateTime) : CollectionDetailScreenEvent
    data class ShowDeleteConfirmation(val timestamp: Timestamp) : CollectionDetailScreenEvent
    data object ConfirmDelete : CollectionDetailScreenEvent
    data object DismissDeleteConfirmation : CollectionDetailScreenEvent
    data object DismissDialog : CollectionDetailScreenEvent
    data object Retry : CollectionDetailScreenEvent
    data class AddComment(val timestamp: Timestamp, val comment: String): CollectionDetailScreenEvent
} 