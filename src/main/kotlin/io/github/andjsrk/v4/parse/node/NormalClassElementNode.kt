package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec

sealed interface NormalClassElementNode: ClassElementNode, NonAtomicNode {
    @EsSpec("IsStatic")
    val isStatic: Boolean
}
