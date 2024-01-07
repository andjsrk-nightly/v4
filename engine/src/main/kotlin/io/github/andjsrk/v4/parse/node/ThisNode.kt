package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.findThisEnvironment
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.NonEmptyOrAbrupt
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ThisNode(override val range: Range): ExpressionNode {
    override fun toString() =
        stringifyLikeDataClass(::range)
    override fun evaluate(): NonEmptyOrAbrupt {
        val env = findThisEnvironment() ?: return throwError(TypeErrorKind.THIS_CANNOT_BE_ACCESSED_IN_GLOBAL)
        return env.getThisBinding()
    }
}
