package com.muthuraj.cycle.fill

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.OkHttpClient

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun isDebug() = true

actual fun provideHttpClient(): HttpClient {
    return HttpClient(OkHttp) {
        setup()

        engine {
            preconfigured =
                OkHttpClient().newBuilder()
                    .hostnameVerifier { _, _ -> true }
                    .build()
        }
    }
}