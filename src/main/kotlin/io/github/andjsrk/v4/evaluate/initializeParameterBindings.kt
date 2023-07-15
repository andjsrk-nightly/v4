package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.CompilerFalsePositive
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.neverHappens
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
                        var value = when {
                            argsIterator.hasNext() -> argsIterator.next()
                            defaultExpr == null -> neverHappens() // this case will be handled on `instantiateFunctionDeclaration`
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
                    else ->
                        @CompilerFalsePositive
                        neverHappens()
                }
            }
            is RestNode -> {
                when (val binding = element.binding) {
                    is IdentifierNode -> {
                        val ref = resolveBinding(binding.stringValue, env)
                        val values = mutableListOf<LanguageType>()
                        argsIterator.forEach { values += it }
                        val arr = ArrayType.from(values)
                        ref.putOrInitializeBinding(arr, env)
                    }
                    is BindingPatternNode -> TODO()
                    else ->
                        @CompilerFalsePositive
                        neverHappens()
                }
            }
        }
    }
    return empty
}
