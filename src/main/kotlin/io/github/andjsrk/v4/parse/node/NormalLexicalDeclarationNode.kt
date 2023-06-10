package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.evaluate.type.spec.Completion
import io.github.andjsrk.v4.evaluate.type.spec.Reference
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
    override fun evaluate(): Completion {
        for (binding in bindings) {
            when (binding.binding) {
                is IdentifierNode -> {
                    val name = binding.binding.stringValue
                    val lhs = neverAbrupt<Reference>(resolveBinding(name))
                    val value = binding.run {
                        when {
                            value == null -> NullType
                            value.isAnonymous -> getLanguageTypeOrReturn(value.evaluateWithName(name)) { return it }
                            else -> value.evaluateValueOrReturn { return it }
                        }
                    }
                    lhs.initializeBinding(value)
                }
                is BindingPatternNode -> TODO()
            }
        }
        return Completion.empty
    }
}
