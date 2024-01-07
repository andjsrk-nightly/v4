package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

sealed class PrimitiveLiteralNode(val raw: String, override val range: Range): LiteralNode
