package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.builtin.Generator
import io.github.andjsrk.v4.evaluate.builtin.sealedData
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.toNormal
import io.github.andjsrk.v4.not

fun createIteratorObject(nextMethod: FunctionType, returnMethod: FunctionType? = null): ObjectType {
    val obj = ObjectType(properties=mutableMapOf(
        *listOfNotNull(
            "next".sealedData(nextMethod),
            returnMethod?.let { "return".sealedData(it) },
        )
            .toTypedArray()
    ))
    return Generator.construct(listOf(obj))
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
                    .toNormal()
            } else {
                createIteratorResult(NullType, true)
                    .toNormal()
            }
        }
    )
}
