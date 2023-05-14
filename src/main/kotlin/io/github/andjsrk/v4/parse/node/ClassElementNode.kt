package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.parse.ClassElementKind

sealed interface ClassElementNode: Node {
    @EsSpec("ClassElementKind")
    val kind get() =
        when (this) {
            is ConstructorNode -> ClassElementKind.CONSTRUCTOR
            is EmptyStatementNode -> null
            else -> ClassElementKind.NON_CONSTRUCTOR
        }
}
