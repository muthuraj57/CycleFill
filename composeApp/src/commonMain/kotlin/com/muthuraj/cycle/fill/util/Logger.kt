// $Id$
package com.muthuraj.cycle.fill.util

import com.muthuraj.cycle.fill.isDebug

/**
 * Created by Muthuraj on 27/05/21.
 */
inline fun Any.log(
    showLog: Boolean = true,
    logger: () -> String,
) {
    if(showLog && isDebug()){
        Logger.log(
            this::class.simpleName.orEmpty() + ":${hashCode()}",
            logger()
        )
    }
}

inline fun Any.logI(
    showLog: Boolean = true,
    logger: () -> String,
) {
    if(showLog && isDebug()){
        Logger.logI(
            this::class.simpleName.orEmpty() + ":${hashCode()}",
            logger()
        )
    }
}

inline fun Any.logE(
    showLog: Boolean = true,
    logger: () -> String,
) {
    if(showLog && isDebug()){
        Logger.logE(
            this::class.simpleName.orEmpty() + ":${hashCode()}",
            logger()
        )
    }
}

inline fun Any.logV(
    showLog: Boolean = true,
    logger: () -> String,
) {
    if(showLog && isDebug()){
        Logger.logV(
            this::class.simpleName.orEmpty() + ":${hashCode()}",
            logger()
        )
    }
}

inline fun Any.logW(
    showLog: Boolean = true,
    logger: () -> String,
) {
    if(showLog && isDebug()){
        Logger.logW(
            this::class.simpleName.orEmpty() + ":${hashCode()}",
            logger()
        )
    }
}

expect object Logger {
    fun log(
        tag: String,
        message: String,
    )

    fun logI(
        tag: String,
        message: String,
    )

    fun logW(
        tag: String,
        message: String,
    )

    fun logE(
        tag: String,
        message: String,
    )

    fun logV(
        tag: String,
        message: String,
    )
}

fun Throwable.printDebugStackTrace() {
    if (isDebug()) {
        printStackTrace()
    }
}
