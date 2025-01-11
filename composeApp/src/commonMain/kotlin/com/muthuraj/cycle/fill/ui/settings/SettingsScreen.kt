/* $Id$ */
package com.muthuraj.cycle.fill.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cyclefill.composeapp.generated.resources.Res
import cyclefill.composeapp.generated.resources.arrow_right
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/**
 * Created by Muthuraj on 11/01/25.
 */
@Composable
fun SettingsScreen(screenState: SettingsScreenState, doAction: (SettingsScreenEvent) -> Unit) {
    val scope = rememberCoroutineScope()
    val scaffoldState =
        rememberBottomSheetScaffoldState(rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed))
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
        sheetContent = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Theme.entries.forEach { theme ->
                    val onClick = {
                        doAction(SettingsScreenEvent.OnThemeChanged(theme))
                        scope.launch {
                            scaffoldState.bottomSheetState.collapse()
                        }
                        Unit
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable(onClick = onClick)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = theme == screenState.theme, onClick = onClick)
                        Text(theme.name)
                    }
                }
                Button(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.collapse()
                        }
                    }) {
                    Text("Close")
                }
            }
        }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable(onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    })
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(modifier = Modifier.weight(1f), text = "App theme")
                Text(
                    screenState.theme.name,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.56f)
                )
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(Res.drawable.arrow_right),
                    contentDescription = null,
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.56f)
                )
            }
        }
    }
}