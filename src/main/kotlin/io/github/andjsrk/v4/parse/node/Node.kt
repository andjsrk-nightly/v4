package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.type.Completion

sealed interface Node {
    val range: Range
    override fun toString(): String
    fun evaluate(): Completion = TODO() // temp
}
