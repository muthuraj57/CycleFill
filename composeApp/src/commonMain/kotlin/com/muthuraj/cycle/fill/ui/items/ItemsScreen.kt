package com.muthuraj.cycle.fill.ui.items

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.muthuraj.cycle.fill.util.compose.DaysElapsedChip
import com.muthuraj.cycle.fill.util.compose.ErrorWithRetry
import com.muthuraj.cycle.fill.util.compose.SearchField
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ItemsScreen(
    collectionName: String,
    screenState: ItemsScreenState,
    doAction: (ItemsScreenEvent) -> Unit,
    onDataUpdated: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler(
        enabled = (screenState as? ItemsScreenState.Success)?.isSelectionMode == true
    ) {
        doAction(ItemsScreenEvent.ExitSelectionMode)
    }
    Scaffold(
        topBar = {
            var showSearchBar by remember { mutableStateOf(false) }
            val selectionState =
                (screenState as? ItemsScreenState.Success)?.takeIf { it.isSelectionMode }
            if (selectionState != null) {
                SelectionTopBar(
                    screenState = selectionState,
                    doAction = doAction
                )
            } else TopAppBar(
                title = {
                    if (showSearchBar) {
                        SearchField(onSearch = {
                            doAction(ItemsScreenEvent.Search(it))
                        })
                    } else {
                        Text(collectionName)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (screenState is ItemsScreenState.Success && screenState.dates.isNotEmpty()) {
                        if (showSearchBar) {
                            IconButton(onClick = {
                                showSearchBar = false
                                doAction(ItemsScreenEvent.Search(""))
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Search",
                                )
                            }
                        } else {
                            IconButton(onClick = { showSearchBar = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                )
                            }
                        }
                        IconButton(onClick = { doAction(ItemsScreenEvent.ShowExport) }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export as JSON",
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (screenState is ItemsScreenState.Success && screenState.dates.isNotEmpty() && !screenState.isSelectionMode) {
                FloatingActionButton(
                    onClick = { doAction(ItemsScreenEvent.AddDateClicked) }
                ) {
                    Icon(Icons.Default.Add, "Add Date")
                }
            }
        }
    ) { paddingValues ->
        when (screenState) {
            is ItemsScreenState.Error -> {
                ErrorWithRetry(
                    error = screenState.message,
                    onRetryClick = { doAction(ItemsScreenEvent.Retry) }
                )
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
                        onConfirm = { dateTime ->
                            doAction(ItemsScreenEvent.AddDate(dateTime))
                            onDataUpdated()
                        }
                    )
                }

                screenState.exportJson?.let { json ->
                    ExportJsonDialog(
                        json = json,
                        onDismiss = { doAction(ItemsScreenEvent.DismissExport) }
                    )
                }

                screenState.deleteConfirmation?.let {
                    DeleteConfirmationDialog(
                        onDismiss = { doAction(ItemsScreenEvent.DismissDeleteConfirmation) },
                        onConfirm = {
                            doAction(ItemsScreenEvent.ConfirmDelete)
                            onDataUpdated()
                        }
                    )
                }

                if (screenState.dates.isEmpty()) {
                    EmptyState(
                        collectionName = screenState.collectionName,
                        onAddClick = { doAction(ItemsScreenEvent.AddDateClicked) }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Spacer(Modifier.padding(8.dp))
                        }
                        items(screenState.dates, key = { it.id }) { item ->
                            DateItem(
                                item = item,
                                index = screenState.dates.size - screenState.dates.indexOf(item),
                                selectionMode = screenState.isSelectionMode,
                                isSelected = item.id in screenState.selectedItemIds,
                                onLongClick = {
                                    doAction(ItemsScreenEvent.EnterSelectionMode(item.id))
                                },
                                onToggleSelection = {
                                    doAction(ItemsScreenEvent.ToggleSelection(item.id))
                                },
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
                                    onDataUpdated()
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

@Composable
private fun SelectionTopBar(
    screenState: ItemsScreenState.Success,
    doAction: (ItemsScreenEvent) -> Unit
) {
    TopAppBar(
        title = { Text("${screenState.selectedItemIds.size} selected") },
        navigationIcon = {
            IconButton(onClick = { doAction(ItemsScreenEvent.ExitSelectionMode) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit selection",
                )
            }
        },
        actions = {
            val anchorId = screenState.selectionAnchorId
            if (screenState.dates.firstOrNull()?.id != anchorId) {
                IconButton(onClick = { doAction(ItemsScreenEvent.SelectUpToTop) }) {
                    Icon(
                        imageVector = Icons.Default.VerticalAlignTop,
                        contentDescription = "Select up to top",
                    )
                }
            }
            if (screenState.dates.lastOrNull()?.id != anchorId) {
                IconButton(onClick = { doAction(ItemsScreenEvent.SelectUpToBottom) }) {
                    Icon(
                        imageVector = Icons.Default.VerticalAlignBottom,
                        contentDescription = "Select down to bottom",
                    )
                }
            }
            IconButton(onClick = { doAction(ItemsScreenEvent.ShowExport) }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Export selected as JSON",
                )
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DateItem(
    item: Item,
    index: Int,
    selectionMode: Boolean,
    isSelected: Boolean,
    onLongClick: () -> Unit,
    onToggleSelection: () -> Unit,
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
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (selectionMode) onToggleSelection() },
                onLongClick = {
                    if (selectionMode) onToggleSelection() else onLongClick()
                }
            ),
        backgroundColor = if (isSelected) {
            MaterialTheme.colors.primary.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colors.surface
        },
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
                if (selectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelection() },
                        modifier = Modifier.align(Alignment.Top).size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
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
                if (!selectionMode) Row(
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

@Composable
private fun ExportJsonDialog(
    json: String,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 24.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Export as JSON",
                    style = MaterialTheme.typography.h6
                )
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    SelectionContainer {
                        Text(
                            text = json,
                            style = MaterialTheme.typography.caption,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(json))
                            onDismiss()
                        }
                    ) {
                        Text("Copy")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
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