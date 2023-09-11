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
                        val value = element.getArgValueOrDefault(argsIterator, binding.stringValue)
                            .orReturn { return it }
                        ref.putOrInitializeBinding(value, env)
                            .orReturn { return it }
                    }
                    is ArrayBindingPatternNode -> {
                        val value = element.getArgValueOrDefault(argsIterator)
                        TODO()
                    }
                    is ObjectBindingPatternNode -> {
                        TODO()
                    }
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
                        val arr = ImmutableArrayType.from(values)
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
private fun NonRestNode.getArgValueOrDefault(argsIterator: Iterator<LanguageType>, paramName: StringType? = null): NonEmptyNormalOrAbrupt {
    val value = when {
        argsIterator.hasNext() -> argsIterator.next()
        default == null -> neverHappens() // this case will be handled on `instantiateFunctionDeclaration`
        else -> NullType
    }
    return (
        if (value == NullType && default != null) {
            if (paramName != null && default.isAnonymous) default.evaluateWithName(paramName)
            else default.evaluateValue()
                .orReturn { return it }
        } else value
    )
        .toNormal()
}
