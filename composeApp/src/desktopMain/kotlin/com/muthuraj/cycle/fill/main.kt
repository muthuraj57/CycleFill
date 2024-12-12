package com.muthuraj.cycle.fill

import android.app.Application
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.google.firebase.FirebasePlatform
import com.muthuraj.cycle.fill.ui.App
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

fun main() = application {

    FirebasePlatform.initializeFirebasePlatform(object : FirebasePlatform() {

        val storage = mutableMapOf<String, String>()
        override fun clear(key: String) {
            storage.remove(key)
        }

        override fun log(msg: String) {
            println(msg)
        }

        override fun retrieve(key: String) = storage[key]

        override fun store(key: String, value: String) = storage.set(key, value)
    })

    //add your own values
    val options = FirebaseOptions(
        apiKey = "",
        projectId = "",
        applicationId = "",
    )
    Firebase.initialize(Application(), options)

    Window(
        onCloseRequest = ::exitApplication,
        title = "CycleFill",
    ) {
        App()
    }
}