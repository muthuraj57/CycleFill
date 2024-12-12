/* $Id$ */
package com.muthuraj.cycle.fill.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLog

/**
 * Created by Muthuraj on 08/12/24.
 */
actual object Logger{
    private val time: String
        get() {
            return NSDateFormatter()
                .apply {
                    dateFormat = "HH:mm:ss:SSS"
                }.stringFromDate(NSDate())
        }

    actual fun log(
        tag: String,
        message: String,
    ) {
        NSLog("$time KMM_Ios: $tag: $message")
    }

    actual fun logI(
        tag: String,
        message: String,
    ) {
        NSLog("$time KMM_Ios: $tag: $message")
    }

    actual fun logW(
        tag: String,
        message: String,
    ) {
        NSLog("$time KMM_Ios: $tag: $message")
    }

    actual fun logE(
        tag: String,
        message: String,
    ) {
        NSLog("$time KMM_Ios: $tag: $message")
    }

    actual fun logV(
        tag: String,
        message: String,
    ) {
        NSLog("$time KMM_Ios: $tag: $message")
    }
}