package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ThisNode(override val range: Range): ExpressionNode {
    override fun toString() =
        stringifyLikeDataClass(::range)
    override fun evaluate() = lazyFlow f@ {
        val env = findThisEnvironment() ?: return@f throwError(TypeErrorKind.THIS_CANNOT_BE_ACCESSED_IN_GLOBAL)
        env.getThisBinding()
    }
}
