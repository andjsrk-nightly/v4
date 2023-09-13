package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.CompilerFalsePositive
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.neverHappens
import io.github.andjsrk.v4.parse.isAnonymous
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.stringValue

internal fun UniqueFormalParametersNode.initializeParameterBindings(argsIterator: Iterator<NonEmptyNormalOrAbrupt>, env: Environment?) =
    elements.initializeParameterBindings(argsIterator, env)
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
                    }
                    is ArrayBindingPatternNode -> TODO()
                    is ObjectBindingPatternNode -> TODO()
                    else ->
                        @CompilerFalsePositive
                        neverHappens()
                }
            }
            is NonRestNode -> {
                when (val binding = element.binding) {
                    is IdentifierNode -> {
                        val ref = resolveBinding(binding.stringValue, env)
                        val value = element.getArgValueOrDefault(valuesIterator, size, i, binding.stringValue)
                            .orReturn { return it }
                        ref.putOrInitializeBinding(value, env)
                            .orReturn { return it }
                    }
                    is ArrayBindingPatternNode -> {
                        val value = element.getArgValueOrDefault(valuesIterator, size, i)
                            .orReturn { return it }
                        val valueIter = iterableToSequence(value)
                            .orReturn { return it }
                            .value
                            .iterator()
                        val iter = iterator {
                            for (elem in binding.elements) {
                                when (elem) {
                                    is NonRestNode ->
                                        if (valueIter.hasNext()) yield(valueIter.next())
                                        else return@iterator
                                    is RestNode -> {
                                        val values = valueIter.toLanguageValueList()
                                            .orReturn {
                                                yield(it)
                                                return@iterator
                                            }
                                        yield(
                                            ImmutableArrayType(values)
                                                .toNormal()
                                        )
                                    }
                                }
                            }
                        }
                        binding.elements.initializeParameterBindings(iter, env)
                            .orReturn { return it }
                    }
                    is ObjectBindingPatternNode -> {
                        val value = element.getArgValueOrDefault(valuesIterator, size, i)
                            .orReturn { return it }
                        val keys = mutableListOf<PropertyKey>()
                        val iter = binding.elements
                            .asSequence()
                            .map {
                                when (it) {
                                    is RestNode -> {
                                        val maybeObj = value as? ObjectType
                                        val rest = maybeObj?.run {
                                            val props = properties.toMutableMap() // clone the original object first
                                            // syntactically rest element must be the last,
                                            // so we can sure that appropriate keys are added to the list `key`
                                            for (key in keys) props.remove(key)
                                            ObjectType.createNormal(props)
                                        } ?: ObjectType.createNormal() // since primitive values cannot have any own properties, we can sure that the result is an empty object
                                        rest.toNormal() // an object that contains the other own enumerable properties of the value
                                    }
                                    is NonRestObjectPropertyNode -> {
                                        val key = it.key.toPropertyKey()
                                            .orReturn { return@map it }
                                        keys += key
                                        value.getProperty(key)
                                    }
                                    is NonRestNode -> neverHappens()
                                }
                            }
                            .iterator()
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
private fun NonRestNode.getArgValueOrDefault(
    argsIterator: Iterator<NonEmptyNormalOrAbrupt>,
    expectedCount: Int,
    index: Int,
    paramName: StringType? = null,
): NonEmptyNormalOrAbrupt {
    val value = when {
        argsIterator.hasNext() ->
            argsIterator.next()
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
