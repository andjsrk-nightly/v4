package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.empty
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.parse.*

class NormalLexicalDeclarationNode(
    override val kind: LexicalDeclarationKind,
    val bindings: List<LexicalBindingNode>,
    startRange: Range,
    semicolonRange: Range?,
): LexicalDeclarationNode {
    override val childNodes = bindings
    override val range = startRange..bindings.last().range.extendCarefully(semicolonRange)
    override fun toString() =
        stringifyLikeDataClass(::kind, ::bindings, ::range)
    override fun evaluate() = lazyFlow f@ {
        for (binding in bindings) {
            val bindingElement = binding.element
            val name = (bindingElement as? IdentifierNode)?.stringValue
            val value = binding.run {
                when {
                    value == null -> NullType
                    name != null && value.isAnonymous -> value.evaluateWithName(name)
                    else -> yieldAll(value.evaluateValue())
                        .orReturn { return@f it }
                }
            }
            yieldAll(bindingElement.initializeBy(value, runningExecutionContext.lexicalEnv))
                .orReturn { return@f it }
        }
        empty
    }
}
