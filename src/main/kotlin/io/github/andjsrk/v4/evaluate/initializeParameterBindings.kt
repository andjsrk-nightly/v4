package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.parse.isAnonymous
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.stringValue

internal fun UniqueFormalParametersNode.initializeParameterBindings(argsIterator: Iterator<NonEmptyNormalOrAbrupt>, env: Environment?) =
    elements.initializeParameterBindings(
        argsIterator
            .withGeneratorReturnValue(null)
            .toRestCollectedArrayIterator(elements),
        env,
    )
private fun List<MaybeRestNode>.initializeParameterBindings(valuesIterator: Iterator<NonEmptyNormalOrAbrupt>, env: Environment?): EmptyOrAbrupt {
    for ((i, element) in this.withIndex()) {
        when (element) {
            is RestNode -> {
                when (val binding = element.binding) {
                    is IdentifierNode -> {
                        val ref = resolveBinding(binding.stringValue, env)
                        val value = valuesIterator.next()
                            .orReturn { return it }
                        ref.putOrInitializeBinding(value, env)
                            .orReturn { return it }
                    }
                    is ArrayBindingPatternNode -> {
                        val arr = valuesIterator.next()
                            .orReturn { return it }
                        require(arr is ArrayType)
                        val iter = arr.array
                            .mapAsSequence { it.toNormal() }
                            .iterator()
                            .toRestCollectedArrayIterator(binding.elements)
                        binding.elements.initializeParameterBindings(iter, env)
                    }
                    is ObjectBindingPatternNode -> {
                        val arr = valuesIterator.next()
                            .orReturn { return it }
                        val iter = arr.toRestCollectedObjectIterator(binding.elements)
                        binding.elements.initializeParameterBindings(iter, env)
                            .orReturn { return it }
                    }
                    else ->
                        @CompilerFalsePositive
                        neverHappens()
                }
            }
            is NonRestNode -> {
                when (val binding = element.binding) {
                    is IdentifierNode -> {
                        val ref = resolveBinding(binding.stringValue, env)
                        val value = element.getValueOrDefault(valuesIterator, size, i, binding.stringValue)
                            .orReturn { return it }
                        ref.putOrInitializeBinding(value, env)
                            .orReturn { return it }
                    }
                    is ArrayBindingPatternNode -> {
                        val value = element.getValueOrDefault(valuesIterator, size, i)
                            .orReturn { return it }
                        val valueIter = iterableToSequence(value)
                            .orReturn { return it }
                            .value
                            .iterator()
                        val iter = valueIter.toRestCollectedArrayIterator(binding.elements)
                        binding.elements.initializeParameterBindings(iter, env)
                            .orReturn { return it }
                    }
                    is ObjectBindingPatternNode -> {
                        val value = element.getValueOrDefault(valuesIterator, size, i)
                            .orReturn { return it }
                        val iter = value.toRestCollectedObjectIterator(binding.elements)
                        binding.elements.initializeParameterBindings(iter, env)
                            .orReturn { return it }
                    }
                    else ->
                        @CompilerFalsePositive
                        neverHappens()
                }
            }
        }
    }
    return empty
}
private fun NonRestNode.getValueOrDefault(
    valuesIterator: Iterator<NonEmptyNormalOrAbrupt>,
    expectedCount: Int,
    index: Int,
    paramName: StringType? = null,
): NonEmptyNormalOrAbrupt {
    val value = when {
        valuesIterator.hasNext() ->
            valuesIterator.next()
                .orReturn { return it }
        default == null -> return throwError(TypeErrorKind.ITERABLE_YIELDED_INSUFFICIENT_NUMBER_OF_VALUES, expectedCount.toString(), index.toString())
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
