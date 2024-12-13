package com.muthuraj.cycle.fill.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.muthuraj.cycle.fill.util.compose.DaysElapsedChip
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun ItemsScreen(
    screenState: ItemsScreenState,
    doAction: (ItemsScreenEvent) -> Unit
) {
    when (screenState) {
        is ItemsScreenState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = screenState.message)
            }
        }

        ItemsScreenState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is ItemsScreenState.Success -> {
            if (screenState.showAddDialog) {
                AddDateDialog(
                    onDismiss = { doAction(ItemsScreenEvent.DismissDialog) },
                    onConfirm = { dateTime -> doAction(ItemsScreenEvent.AddDate(dateTime)) }
                )
            }

            screenState.deleteConfirmation?.let {
                DeleteConfirmationDialog(
                    onDismiss = { doAction(ItemsScreenEvent.DismissDeleteConfirmation) },
                    onConfirm = { doAction(ItemsScreenEvent.ConfirmDelete) }
                )
            }

            if (screenState.dates.isEmpty()) {
                EmptyState(
                    collectionName = screenState.collectionName,
                    onAddClick = { doAction(ItemsScreenEvent.AddDateClicked) }
                )
            } else {
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { doAction(ItemsScreenEvent.AddDateClicked) }
                        ) {
                            Icon(Icons.Default.Add, "Add Date")
                        }
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = screenState.collectionName,
                            style = MaterialTheme.typography.h6,
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Spacer(Modifier.padding(6.dp))
                            }
                            items(screenState.dates) { item ->
                                DateItem(
                                    item = item,
                                    index = screenState.dates.size - screenState.dates.indexOf(item),
                                    onDelete = { id ->
                                        doAction(
                                            ItemsScreenEvent.ShowDeleteConfirmation(id)
                                        )
                                    },
                                    onEdit = { id, comment ->
                                        doAction(
                                            ItemsScreenEvent.AddComment(
                                                itemId = id,
                                                comment = comment
                                            )
                                        )
                                    }
                                )
                            }
                            item {
                                Spacer(Modifier.padding(6.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DateItem(
    item: Item,
    index: Int,
    onDelete: (Int) -> Unit,
    onEdit: (Int, String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditCommentDialog(
            initialComment = item.comment,
            date = item.date,
            onDismiss = { showEditDialog = false },
            onConfirm = { comment ->
                onEdit(item.id, comment)
                showEditDialog = false
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ).align(Alignment.Top),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.date,
                        style = MaterialTheme.typography.subtitle1
                    )
                    if (item.comment.isNullOrBlank().not()) {
                        Text(
                            text = item.comment!!,
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = item.weekDay,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                item.daysAgoForLastCycle?.let { days ->
                    DaysElapsedChip(
                        daysElapsed = days,
                        text = "${item.daysAgoForLastCycle.second} cycle"
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { showEditDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colors.error
                        )
                    }

                    IconButton(
                        onClick = { onDelete(item.id) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colors.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditCommentDialog(
    initialComment: String?,
    date: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var comment by remember { mutableStateOf(initialComment.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Comment") },
        text = {
            OutlinedTextField(
                comment, onValueChange = {
                    comment = it
                }, label = {
                    Text("Comment for $date")
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(comment)
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDateDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val date =
                        Instant.fromEpochMilliseconds(datePickerState.selectedDateMillis!!)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date
                            .toString()
                    onConfirm(date)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Entry") },
        text = { Text("Are you sure you want to delete this entry?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colors.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EmptyState(
    collectionName: String,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = collectionName,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "No entries yet",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Add First Entry")
        }
    }
}