package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.EmptyOrAbrupt
import io.github.andjsrk.v4.evaluate.type.Environment
import io.github.andjsrk.v4.evaluate.type.empty
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.parse.isAnonymous
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.stringValue

internal fun UniqueFormalParametersNode.initializeParameterBindings(argsIterator: Iterator<LanguageType>, env: Environment?) =
    elements.initializeParameterBindings(argsIterator, env)
private fun List<MaybeRestNode>.initializeParameterBindings(argsIterator: Iterator<LanguageType>, env: Environment?): EmptyOrAbrupt {
    for (element in this) {
        when (element) {
            is NonRestNode -> {
                when (val binding = element.binding) {
                    is BindingPatternNode -> TODO()
                    is IdentifierNode -> {
                        val ref = resolveBinding(binding.stringValue, env)
                        var value =
                            when {
                                argsIterator.hasNext() -> argsIterator.next()
                                element.default == null -> return Completion.Throw(NullType/* TypeError */)
                                else -> NullType
                            }
                        if (value == NullType && element.default != null) {
                            val defaultValue =
                                if (element.default.isAnonymous) element.default.evaluateWithNameOrReturn(binding.stringValue) { return it }
                                else element.default.evaluateValueOrReturn { return it }
                            value = defaultValue
                        }
                        env.putOrInitializeBinding(ref, value)
                    }
                }
            }
            is RestNode -> TODO()
        }
    }
    return empty
}
