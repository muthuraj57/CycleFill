package com.muthuraj.cycle.fill.ui.collections

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.muthuraj.cycle.fill.models.Collection
import com.muthuraj.cycle.fill.util.compose.DaysElapsedChip

@Composable
fun CollectionsScreen(
    screenState: CollectionsScreenState,
    doAction: (CollectionsScreenEvent) -> Unit
) {
    when (screenState) {
        is CollectionsScreenState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = screenState.message)
            }
        }

        CollectionsScreenState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is CollectionsScreenState.Success -> {
            if (screenState.showAddDialog) {
                AddCollectionDialog(
                    onDismiss = { doAction(CollectionsScreenEvent.DismissDialog) },
                    onConfirm = { name -> doAction(CollectionsScreenEvent.AddCollection(name)) }
                )
            }

            screenState.deleteConfirmation?.let {
                DeleteConfirmationDialog(
                    onDismiss = { doAction(CollectionsScreenEvent.DismissDeleteConfirmation) },
                    onConfirm = { doAction(CollectionsScreenEvent.ConfirmDelete) }
                )
            }

            if (screenState.collections.isEmpty()) {
                EmptyState(
                    itemName = screenState.itemName,
                    onAddClick = { doAction(CollectionsScreenEvent.AddCollectionClicked) }
                )
            } else {
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { doAction(CollectionsScreenEvent.AddCollectionClicked) }
                        ) {
                            Icon(Icons.Default.Add, "Add Tracking")
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
                            text = "Tracking Items",
                            style = MaterialTheme.typography.subtitle2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Spacer(Modifier.padding(6.dp))
                            }
                            items(screenState.collections) { collection ->
                                CollectionItem(
                                    collection = collection,
                                    onClick = {
                                        doAction(
                                            CollectionsScreenEvent.CollectionClicked(
                                                collection
                                            )
                                        )
                                    },
                                    onDelete = {
                                        doAction(
                                            CollectionsScreenEvent.ShowDeleteConfirmation(
                                                collection
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
private fun CollectionItem(
    collection: Collection,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.subtitle1
                )
                if (collection.lastRefillDate != null) {
                    Text(
                        text = "Last: ${collection.lastRefillDate}",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                if (collection.daysElapsed != null) {
                    val text = when (collection.daysElapsed.first) {
                        1 -> {
                            "Yesterday"
                        }

                        0 -> {
                            "Today"
                        }

                        else -> {
                            "${collection.daysElapsed.second} ago"
                        }
                    }
                    DaysElapsedChip(collection.daysElapsed, text = text)
                }
                if (collection.lastRefillDate != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear History",
                            tint = MaterialTheme.colors.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    itemName: String,
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
            text = itemName,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "No tracking items yet",
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
            Text("Add First Item")
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear History") },
        text = {
            Column {
                Text("Are you sure you want to clear all history for this tracking item?")
                Text(
                    text = "This will remove all recorded dates and comments but keep the item.",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colors.primary
                )
            ) {
                Text("Clear")
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
private fun AddCollectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Tracking Item") },
        text = {
            Column {
                Text(
                    text = "Add what you want to track (e.g., Medicine Intake, Filter Change, Cleaning)",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 