package com.muthuraj.cycle.fill.ui.itemdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
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
import com.muthuraj.cycle.fill.models.ItemCollection
import com.muthuraj.cycle.fill.util.compose.DaysElapsedChip
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun ItemDetailScreen(
    screenState: ItemDetailScreenState,
    doAction: (ItemDetailScreenEvent) -> Unit
) {
    when (screenState) {
        is ItemDetailScreenState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = screenState.message)
            }
        }

        ItemDetailScreenState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is ItemDetailScreenState.Success -> {
            if (screenState.showAddDialog) {
                AddCollectionDialog(
                    onDismiss = { doAction(ItemDetailScreenEvent.DismissDialog) },
                    onConfirm = { name -> doAction(ItemDetailScreenEvent.AddCollection(name)) }
                )
            }

            screenState.deleteConfirmation?.let {
                DeleteConfirmationDialog(
                    onDismiss = { doAction(ItemDetailScreenEvent.DismissDeleteConfirmation) },
                    onConfirm = { doAction(ItemDetailScreenEvent.ConfirmDelete) }
                )
            }

            if (screenState.collections.isEmpty()) {
                EmptyState(
                    itemName = screenState.itemName,
                    onAddClick = { doAction(ItemDetailScreenEvent.AddCollectionClicked) }
                )
            } else {
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { doAction(ItemDetailScreenEvent.AddCollectionClicked) }
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
                            text = screenState.itemName,
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
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
                                            ItemDetailScreenEvent.CollectionClicked(
                                                collection
                                            )
                                        )
                                    },
                                    onDelete = {
                                        doAction(
                                            ItemDetailScreenEvent.ShowDeleteConfirmation(
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
    collection: ItemCollection,
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
                    DaysElapsedChip(collection.daysElapsed, suffix = "ago")
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