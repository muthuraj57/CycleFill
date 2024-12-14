package com.muthuraj.cycle.fill

import android.os.Build
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.OkHttpClient

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun isDebug() = BuildConfig.DEBUG

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