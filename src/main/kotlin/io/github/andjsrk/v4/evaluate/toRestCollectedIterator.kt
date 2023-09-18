package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.toNormal
import io.github.andjsrk.v4.neverHappens
import io.github.andjsrk.v4.not
import io.github.andjsrk.v4.parse.isAnonymous
import io.github.andjsrk.v4.parse.node.*

/**
 * Transforms the iterator to simple form to initialize a binding easily.
 *
 * - non-rest element: collect the element as it is
 * - rest element: collect the remaining elements as an array
 */
fun Iterator<NonEmptyNormalOrAbrupt>.toRestCollectedArrayIterator(bindingElements: List<MaybeRestNode>) =
    iterator {
        for (elem in bindingElements) {
            when (elem) {
                is NonRestNode ->
                    if (hasNext()) yield(next())
                    else return@iterator
                is RestNode -> {
                    val values = toLanguageValueList()
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

/**
 * Transforms the value to an iterator with simple form to initialize a binding easily.
 *
 * - non-rest element: collect the value of the property
 * - rest element: collect the remaining own enumerable properties as an object
 */
fun LanguageType.toRestCollectedObjectIterator(bindingElements: List<MaybeRestNode>): Iterator<NonEmptyNormalOrAbrupt> {
    val nonRestKeys = mutableSetOf<PropertyKey>()
    return bindingElements
        .asSequence()
        .map { elem ->
            when (elem) {
                is RestNode -> {
                    val maybeObj = this as? ObjectType
                    val rest = maybeObj?.run {
                        val props = properties.toMutableMap() // clone the original object first
                        // syntactically rest element must be the last,
                        // so we can sure that appropriate keys are added to the list `key`
                        props -= nonRestKeys
                        props.entries.removeAll { (_, prop) -> prop.not { enumerable } }
                        ObjectType.createNormal(props)
                    }
                        ?: ObjectType.createNormal() // since primitive values cannot have any own properties, we can sure that the result is an empty object
                    rest.toNormal() // an object that contains the other own enumerable properties of the value
                }
                is NonRestObjectPropertyNode -> {
                    val key = elem.key.toPropertyKey()
                        .orReturn { return@map it }
                    nonRestKeys += key
                    if (!hasProperty(key) && elem.default == null) throwError(TypeErrorKind.REQUIRED_PROPERTY_NOT_FOUND, key.display())
                    else getProperty(key)
                }
                is NonRestNode -> neverHappens()
            }
        }
        .iterator()
}

fun NonRestNode.getValueOrDefault(
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
