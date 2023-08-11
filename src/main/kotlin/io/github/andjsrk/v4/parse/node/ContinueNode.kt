package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.EvalFlow
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ContinueNode(startRange: Range, semicolonRange: Range?): StatementNode {
    override val range = startRange.extendCarefully(semicolonRange)
    override fun toString() =
        stringifyLikeDataClass(::range)
    override fun evaluate() =
        EvalFlow {
            `return`(
                Completion.Continue(null, null)
            )
        }
}
