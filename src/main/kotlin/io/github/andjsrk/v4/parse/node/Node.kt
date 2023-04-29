package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

sealed interface Node {
    val range: Range
    override fun toString(): String
}
