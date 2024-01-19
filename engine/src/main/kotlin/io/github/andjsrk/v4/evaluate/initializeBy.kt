package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.CompilerFalsePositive
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.neverHappens
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.stringValue

fun List<MaybeRestNode>.initializeBy(valuesIterator: Iterator<SimpleLazyFlow<NonEmptyOrAbrupt>>, env: Environment?): LazyFlow<EmptyOrAbrupt, MaybeEmptyOrAbrupt> = lazyFlow f@ {
    for ((i, element) in this@initializeBy.withIndex()) {
        when (element) {
            is RestNode -> {
                when (val binding = element.binding) {
                    is IdentifierNode -> {
                        val ref = resolveBinding(binding.stringValue, env)
                            .orReturn { return@f it }
                        val value = yieldAll(valuesIterator.next())
                            .orReturn { return@f it }
                        ref.putOrInitializeBinding(value, env)
                            .orReturn { return@f it }
                    }
                    is ArrayBindingPatternNode -> {
                        val arr = yieldAll(valuesIterator.next())
                            .orReturn { return@f it }
                        val iter = IteratorRecord.from(arr)
                            .orReturn { return@f it }
                            .toSequence()
                            .iterator()
                            .toRestCollectedArrayIterator(binding.elements)
                            .map {
                                lazyFlow { it }
                            }
                        yieldAll(binding.elements.initializeBy(iter, env))
                            .orReturn { return@f it }
                    }
                    is ObjectBindingPatternNode -> {
                        val arr = yieldAll(valuesIterator.next())
                            .orReturn { return@f it }
                        val iter = arr.toRestCollectedObjectIterator(binding.elements)
                        yieldAll(binding.elements.initializeBy(iter, env))
                            .orReturn { return@f it }
                    }
                    else ->
                        @CompilerFalsePositive
                        neverHappens()
                }
            }
            is NonRestNode -> {
                val value = yieldAll(valuesIterator.nextOrDefault(element, size, i))
                    .orReturn { return@f it }
                yieldAll(element.binding.initializeBy(value, env))
                    .orReturn { return@f it }
            }
        }
    }
    empty
}

fun BindingElementNode.initializeBy(value: LanguageType, env: Environment?): SimpleLazyFlow<MaybeAbrupt<*>> = lazyFlow f@ {
    when (this@initializeBy) {
        is IdentifierNode -> {
            val ref = resolveBinding(stringValue, env)
                .orReturn { return@f it }
            ref.putOrInitializeBinding(value, env)
        }
        is ArrayBindingPatternNode -> {
            val valueIter = IteratorRecord.from(value)
                .orReturn { return@f it }
                .toSequence()
                .iterator()
            val iter = valueIter.toRestCollectedArrayIterator(elements)
                .map {
                    lazyFlow { it }
                }
            yieldAll(elements.initializeBy(iter, env))
        }
        is ObjectBindingPatternNode -> {
            val iter = value.toRestCollectedObjectIterator(elements)
            yieldAll(elements.initializeBy(iter, env))
        }
        else ->
            @CompilerFalsePositive
            neverHappens()
    }
}
