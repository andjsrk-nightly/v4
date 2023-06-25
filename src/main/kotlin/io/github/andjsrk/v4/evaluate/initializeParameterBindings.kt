package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.EmptyOrAbrupt
import io.github.andjsrk.v4.evaluate.type.Environment
import io.github.andjsrk.v4.evaluate.type.empty
import io.github.andjsrk.v4.evaluate.type.lang.ArrayType
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
                    is IdentifierNode -> {
                        val ref = resolveBinding(binding.stringValue, env)
                        val defaultExpr = element.default
                        var value =
                            when {
                                argsIterator.hasNext() -> argsIterator.next()
                                defaultExpr == null -> return Completion.Throw(NullType/* TypeError */)
                                else -> NullType
                            }
                        if (value == NullType && defaultExpr != null) {
                            val defaultValue =
                                if (defaultExpr.isAnonymous) defaultExpr.evaluateWithNameOrReturn(binding.stringValue) { return it }
                                else defaultExpr.evaluateValueOrReturn { return it }
                            value = defaultValue
                        }
                        ref.putOrInitializeBinding(value, env)
                    }
                    is BindingPatternNode -> TODO()
                }
            }
            is RestNode -> {
                when (val binding = element.binding) {
                    is IdentifierNode -> {
                        val ref = resolveBinding(binding.stringValue, env)
                        val values = mutableListOf<LanguageType>()
                        argsIterator.forEach { values += it }
                        val arr = ArrayType(values.size.toLong())
                        for ((i, value) in values.withIndex()) {
                            val indexKey = neverAbrupt(toString(i.toDouble().languageValue))
                            arr.createDataProperty(indexKey, value)
                        }
                        ref.putOrInitializeBinding(arr, env)
                    }
                    is BindingPatternNode -> TODO()
                }
            }
        }
    }
    return empty
}
