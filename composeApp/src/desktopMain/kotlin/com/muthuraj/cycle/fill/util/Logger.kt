/* $Id$ */
package com.muthuraj.cycle.fill.util

/**
 * Created by Muthuraj on 08/12/24.
 */
actual object Logger {
    actual fun log(
        tag: String,
        message: String,
    ) {
        println("$tag: $message")
    }

    actual fun logI(
        tag: String,
        message: String,
    ) {
        println("$tag: $message")
    }

    actual fun logW(
        tag: String,
        message: String,
    ) {
        println("$tag: $message")
    }

    actual fun logE(
        tag: String,
        message: String,
    ) {
        println("$tag: $message")
    }

    actual fun logV(
        tag: String,
        message: String,
    ) {
        println("$tag: $message")
    }
}