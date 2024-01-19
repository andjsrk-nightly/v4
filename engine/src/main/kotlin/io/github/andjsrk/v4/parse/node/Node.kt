package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.SimpleLazyFlow
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt

sealed interface Node {
    val range: Range
    override fun toString(): String
    fun evaluate(): SimpleLazyFlow<MaybeAbrupt<*>>
}
