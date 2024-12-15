package com.muthuraj.cycle.fill

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.JsClient

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun isDebug(): Boolean {
    return true
}

actual fun provideHttpClient(): HttpClient {
    return HttpClient(JsClient()){
        setup()
    }
}