package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.neverHappens
import io.github.andjsrk.v4.not
import io.github.andjsrk.v4.parse.isAnonymous
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.stringValue

/**
 * Transforms the iterator to simple form to initialize a binding easily.
 *
 * - non-rest element: collect the element as it is
 * - rest element: collect the remaining elements as an array
 */
fun Iterator<NonEmptyOrThrow>.toRestCollectedArrayIterator(bindingElements: List<MaybeRestNode>) =
    iterator {
        for (elem in bindingElements) {
            when (elem) {
                is NonRestNode ->
                    if (hasNext()) yield(next())
                    else return@iterator
                is RestNode -> {
                    val values = toLanguageValueList()
                        .orReturnThrow {
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
fun LanguageType.toRestCollectedObjectIterator(bindingElements: List<MaybeRestNode>): Iterator<SimpleLazyFlow<NonEmptyOrAbrupt>> {
    val nonRestKeys = mutableSetOf<PropertyKey>()
    return bindingElements
        .asSequence()
        .map { elem ->
            lazyFlow f@ {
                when (elem) {
                    is RestNode -> {
                        val maybeObj = this@toRestCollectedObjectIterator as? ObjectType
                        val rest = maybeObj?.run {
                            val props = properties.toMutableMap() // clone the original object
                            props -= nonRestKeys // remove properties that its key is contained in non-rest keys
                            props.entries.removeAll { (_, prop) -> prop.not { enumerable } }
                            ObjectType.createNormal(props)
                        }
                            ?: ObjectType.createNormal() // since primitive values cannot have any own properties, we can sure that the result is an empty object
                        rest.toNormal() // an object that contains the other own enumerable properties of the value
                    }
                    is NonRestObjectPropertyNode -> {
                        val key = yieldAll(elem.key.toLanguageTypePropertyKey())
                            .orReturn { return@f it }
                        nonRestKeys += key
                        val hasKey = hasProperty(key)
                            .orReturnThrow { return@f it }
                            .nativeValue
                        if (!hasKey && elem.default == null) throwError(
                            TypeErrorKind.REQUIRED_PROPERTY_NOT_FOUND,
                            key.string()
                        )
                        else getProperty(key)
                    }
                    is NonRestNode -> neverHappens()
                }
            }
        }
        .iterator()
}

fun Iterator<Completion.FromFunctionBody<LanguageType>>.nextOrDefault(
    element: NonRestNode,
    expectedCount: Int,
    index: Int,
) = lazyFlow f@ {
    val iter = this@nextOrDefault
    var tookNext = false
    val value = when {
        iter.hasNext() -> {
            tookNext = true
            iter.next()
                .orReturnNonEmpty { return@f it }
        }
        else -> NullType
    }
    if (!tookNext && element.default == null) return@f throwError(
        TypeErrorKind.ITERABLE_YIELDED_INSUFFICIENT_NUMBER_OF_VALUES,
        expectedCount.toString(),
        index.toString(),
    )
    if (value == NullType && element.default != null) {
        val paramName = (element.binding as? IdentifierNode)?.stringValue
        if (paramName != null && element.default.isAnonymous) element.default.evaluateWithName(paramName)
        else element.default.evaluateValue()
            .asFromFunctionBody()
            .unwrap()
            .orReturnNonEmpty { return@f it }
    } else {
        value
    }
        .toNormal()
}
