package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

interface Node {
    val range: Range
    override fun toString(): String
}
