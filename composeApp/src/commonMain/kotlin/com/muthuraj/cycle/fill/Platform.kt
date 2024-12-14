package com.muthuraj.cycle.fill

import com.muthuraj.cycle.fill.util.log
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun isDebug(): Boolean

expect fun provideHttpClient(): HttpClient

fun HttpClientConfig<*>.setup() {
    val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    install(Logging) {
        logger =
            object : Logger {
                override fun log(message: String) {
                    this@install.log { message }
                }
            }
        level = LogLevel.ALL
    }
    install(ContentNegotiation) {
        json(json, contentType = ContentType.Any)
    }
    expectSuccess = true
}