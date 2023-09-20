package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.stringValue

fun List<MaybeRestNode>.initializeBy(valuesIterator: Iterator<NonEmptyNormalOrAbrupt>, env: Environment?): EmptyOrAbrupt {
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
                        val iter = iterableToSequence(arr)
                            .orReturn { return it }
                            .value
                            .iterator()
                            .toRestCollectedArrayIterator(binding.elements)
                        binding.elements.initializeBy(iter, env)
                            .orReturn { return it }
                    }
                    is ObjectBindingPatternNode -> {
                        val arr = valuesIterator.next()
                            .orReturn { return it }
                        val iter = arr.toRestCollectedObjectIterator(binding.elements)
                        binding.elements.initializeBy(iter, env)
                            .orReturn { return it }
                    }
                }
            }
            is NonRestNode -> {
                val value = valuesIterator.nextOrDefault(element, size, i)
                    .orReturn { return it }
                element.binding.initializeBy(value, env)
                    .orReturn { return it }
            }
        }
    }
    return empty
}

fun BindingElementNode.initializeBy(value: LanguageType, env: Environment?): EmptyOrAbrupt {
    return when (this) {
        is IdentifierNode -> {
            val ref = resolveBinding(stringValue, env)
            ref.putOrInitializeBinding(value, env)
        }
        is ArrayBindingPatternNode -> {
            val valueIter = iterableToSequence(value)
                .orReturn { return it }
                .value
                .iterator()
            val iter = valueIter.toRestCollectedArrayIterator(elements)
            elements.initializeBy(iter, env)
        }
        is ObjectBindingPatternNode -> {
            val iter = value.toRestCollectedObjectIterator(elements)
            elements.initializeBy(iter, env)
        }
    }
}
