package com.muthuraj.cycle.fill.util

import kotlinx.coroutines.flow.Flow

/**
 * Created by Muthuraj on 23/07/21.
 */
fun <T1, T2, R> Flow<T1>.combine(
    flow2: Flow<T2>,
    transform: suspend (T1, T2) -> R,
): Flow<R> =
    kotlinx.coroutines.flow.combine(this, flow2) { t1, t2 ->
        transform(t1, t2)
    }

fun <T1, T2, T3, R> Flow<T1>.combine(
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    transform: suspend (T1, T2, T3) -> R,
): Flow<R> =
    kotlinx.coroutines.flow.combine(this, flow2, flow3) { t1, t2, t3 ->
        transform(t1, t2, t3)
    }

fun <T1, T2, T3, T4, R> Flow<T1>.combine(
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    transform: suspend (T1, T2, T3, T4) -> R,
): Flow<R> =
    kotlinx.coroutines.flow.combine(this, flow2, flow3, flow4) { t1, t2, t3, t4 ->
        transform(t1, t2, t3, t4)
    }

fun <T1, T2, T3, T4, T5, R> Flow<T1>.combine(
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    transform: suspend (T1, T2, T3, T4, T5) -> R,
): Flow<R> =
    kotlinx.coroutines.flow.combine(this, flow2, flow3, flow4, flow5) { t1, t2, t3, t4, t5 ->
        transform(t1, t2, t3, t4, t5)
    }

fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    transform: suspend (T1, T2, T3, T4, T5, T6) -> R,
): Flow<R> =
    kotlinx.coroutines.flow.combine(flow1, flow2, flow3, flow4, flow5, flow6) { values ->
        transform(
            values[0] as T1,
            values[1] as T2,
            values[2] as T3,
            values[3] as T4,
            values[4] as T5,
            values[5] as T6,
        )
    }

