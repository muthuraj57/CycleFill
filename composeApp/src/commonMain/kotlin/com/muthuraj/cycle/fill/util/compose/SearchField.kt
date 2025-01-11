/* $Id$ */
package com.muthuraj.cycle.fill.util.compose

import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color

/**
 * Created by Muthuraj on 11/01/25.
 */
@Composable
fun SearchField(onSearch: (text: String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    var searchText by remember { mutableStateOf("") }
    TextField(
        modifier = Modifier.focusRequester(focusRequester),
        colors = TextFieldDefaults.textFieldColors(cursorColor = Color.White),
        value = searchText,
        onValueChange = {
            searchText = it.trim()
            onSearch(searchText)
        },
        label = { Text("Search here", color = Color.White) }
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}