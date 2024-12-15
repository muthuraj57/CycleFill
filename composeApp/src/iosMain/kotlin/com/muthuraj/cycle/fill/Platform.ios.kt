package com.muthuraj.cycle.fill

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import platform.Foundation.NSURLCredential
import platform.Foundation.NSURLSessionAuthChallengeUseCredential
import platform.UIKit.UIDevice
import kotlin.experimental.ExperimentalNativeApi

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

@OptIn(ExperimentalNativeApi::class)
actual fun isDebug() = kotlin.native.Platform.isDebugBinary

actual fun provideHttpClient(): HttpClient {
    return HttpClient(Darwin) {
        setup()
//        engine {
//            handleChallenge { session, task, challenge, completionHandler ->
//                completionHandler(NSURLSessionAuthChallengeUseCredential.toInt(), NSURLCredential())
//            }
//        }
    }
}