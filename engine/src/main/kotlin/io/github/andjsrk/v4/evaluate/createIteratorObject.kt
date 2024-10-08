package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.builtin.Generator
import io.github.andjsrk.v4.evaluate.builtin.sealedData
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.not

fun createIteratorObject(nextMethod: FunctionType, closeMethod: FunctionType? = null): ObjectType {
    val obj = ObjectType.Impl(
        listOfNotNull(
            "next".sealedData(nextMethod),
            closeMethod?.let { "close".sealedData(it) },
        )
            .toMutableMap()
    )
    return Generator.new(listOf(obj))
        .unwrap()
}

/**
 * @param containsReturnValue Indicates whether the sequence contains return value that comes from a return statement.
 */
fun createIteratorObjectFromSequence(sequence: Sequence<LanguageType>, containsReturnValue: Boolean = false): ObjectType {
    val seqIter = sequence.iterator()
    return createIteratorObject(
        functionWithoutThis("next") { _ ->
            if (seqIter.hasNext()) {
                val value = seqIter.next()
                val done = seqIter.not { hasNext() } && containsReturnValue
                createIteratorResult(value, done)
            } else {
                createIteratorResult(NullType, true)
            }
                .toNormal()
        }
    )
}
