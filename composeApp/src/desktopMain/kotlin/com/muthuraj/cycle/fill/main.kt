package com.muthuraj.cycle.fill

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.muthuraj.cycle.fill.ui.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "CycleFill",
    ) {
        App()
    }
}