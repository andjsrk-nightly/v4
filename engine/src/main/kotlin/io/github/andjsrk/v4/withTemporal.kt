package io.github.andjsrk.v4

import kotlin.reflect.KMutableProperty0

inline fun <R> withTemporalState(
    setState: () -> Unit,
    getBackOriginalState: () -> Unit,
    block: () -> R,
): R {
    setState()
    return try {
        block()
    } finally {
        getBackOriginalState()
    }
}

inline fun <T, R> withTemporalValue(prop: KMutableProperty0<T>, temporalValue: T, block: () -> R): R {
    val originalValue = prop.get()
    return withTemporalState(
        { prop.set(temporalValue) },
        { prop.set(originalValue) },
        block,
    )
}
