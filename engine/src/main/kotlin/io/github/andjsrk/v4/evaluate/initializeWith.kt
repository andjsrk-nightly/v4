package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.stringValue

/**
 * Note that the function accepts an [Iterator] instead of an [IteratorRecord].
 */
@EsSpec("IteratorBindingInitialization")
fun List<MaybeRestNode>.initializeWith(
    valuesIterator: Iterator<Completion.FromFunctionBody<LanguageType>>,
    env: Environment?,
): LazyFlow<EmptyOrNonEmptyAbrupt, Completion.FromFunctionBody<LanguageType?>> = lazyFlow f@ {
    for ((i, element) in this@initializeWith.withIndex()) {
        when (element) {
            is RestNode -> {
                when (val binding = element.binding) {
                    is IdentifierNode -> {
                        val ref = resolveBinding(binding.stringValue, env)
                            .orReturnThrow { return@f it }
                        val value = valuesIterator.next()
                            .orReturnNonEmpty { return@f it }
                        ref.putOrInitializeBinding(value, env)
                            .orReturnThrow { return@f it }
                    }
                    is ArrayBindingPatternNode -> {
                        val arr = valuesIterator.next()
                            .orReturnNonEmpty { return@f it }
                        val iter = IteratorRecord.from(arr)
                            .orReturnThrow { return@f it }
                            .toSequence()
                            .iterator()
                            .toRestCollectedArrayIterator(binding.elements)
                        yieldAll(binding.elements.initializeWith(iter, env))
                            .orReturnNonEmpty { return@f it }
                    }
                    is ObjectBindingPatternNode -> {
                        val arr = valuesIterator.next()
                            .orReturnNonEmpty { return@f it }
                        val iter = arr.toRestCollectedObjectIterator(binding.elements)
                            .map { it.asFromFunctionBody().unwrap() }
                        yieldAll(binding.elements.initializeWith(iter, env))
                            .orReturnNonEmpty { return@f it }
                    }
                    else ->
                        @CompilerFalsePositive
                        neverHappens()
                }
            }
            is NonRestNode -> {
                val value = yieldAll(valuesIterator.nextOrDefault(element, size, i))
                    .orReturnNonEmpty { return@f it }
                yieldAll(element.binding.initializeWith(value, env))
                    .orReturnNonEmpty { return@f it }
            }
        }
    }
    empty
}

@EsSpec("BindingInitialization")
fun BindingElementNode.initializeWith(value: LanguageType, env: Environment?) = lazyFlow f@ {
    when (this@initializeWith) {
        is IdentifierNode -> {
            val ref = resolveBinding(stringValue, env)
                .orReturnThrow { return@f it }
            ref.putOrInitializeBinding(value, env)
        }
        is ArrayBindingPatternNode -> {
            val valueIter = IteratorRecord.from(value)
                .orReturnThrow { return@f it }
                .toSequence()
                .iterator()
            val iter = valueIter.toRestCollectedArrayIterator(elements)
            yieldAll(elements.initializeWith(iter, env))
        }
        is ObjectBindingPatternNode -> {
            val iter = value.toRestCollectedObjectIterator(elements)
                .map {
                    it
                        .asFromFunctionBody()
                        .unwrap()
                }
            yieldAll(elements.initializeWith(iter, env))
        }
        else ->
            @CompilerFalsePositive
            neverHappens()
    }
}
