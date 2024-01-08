package io.github.andjsrk.v4

inline fun <R> Boolean.thenTake(block: Deferred<R>) =
    if (this) block()
    else null
