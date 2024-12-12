/* $Id$ */
package com.muthuraj.cycle.fill.util

import android.util.Log

/**
 * Created by Muthuraj on 08/12/24.
 */
actual object Logger {
    actual fun log(
        tag: String,
        message: String,
    ) {
        Log.d(tag, message)
    }

    actual fun logI(
        tag: String,
        message: String,
    ) {
        Log.i(tag, message)
    }

    actual fun logW(
        tag: String,
        message: String,
    ) {
        Log.w(tag, message)
    }

    actual fun logE(
        tag: String,
        message: String,
    ) {
        Log.e(tag, message)
    }

    actual fun logV(
        tag: String,
        message: String,
    ) {
        Log.v(tag, message)
    }
}