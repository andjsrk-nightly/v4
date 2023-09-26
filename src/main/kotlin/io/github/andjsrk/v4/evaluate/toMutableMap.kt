package io.github.andjsrk.v4.evaluate

inline fun <K, V> Iterable<Pair<K, V>>.toMutableMap() =
    toMap(mutableMapOf())
