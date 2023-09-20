package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*

fun iterableToSequence(value: LanguageType, ignoreReturnValue: Boolean = true): MaybeAbrupt<GeneralSpecValue<Sequence<NonEmptyNormalOrAbrupt>>> {
    val iteratorMethod = value.getMethod(SymbolType.WellKnown.iterator)
        .orReturn { return it }
        ?: return throwError(TypeErrorKind.NOT_ITERABLE)
    val iteratorObj = iteratorMethod.call(value, emptyList())
        .orReturn { return it }
        .requireToBe<ObjectType> { return it }
    return iteratorObjectToSequence(iteratorObj, ignoreReturnValue)
}
fun iteratorObjectToSequence(obj: ObjectType, ignoreReturnValue: Boolean): MaybeAbrupt<GeneralSpecValue<Sequence<NonEmptyNormalOrAbrupt>>> {
    val nextMethod = obj.getMethod("next".languageValue)
        .orReturn { return it }
        ?: return throwError(TypeErrorKind.NOT_AN_ITERATOR)
    return GeneralSpecValue(
            sequence {
            while (true) {
                val callRes = nextMethod.call(obj, emptyList())
                    .orReturn {
                        yield(it)
                        return@sequence
                    }
                    .requireToBe<ObjectType> {
                        yield(it)
                        return@sequence
                    }
                val done = callRes.getIterResultDone()
                    .orReturn {
                        yield(it)
                        return@sequence
                    }
                    .value
                if (done && ignoreReturnValue) break
                val value = callRes.getIterResultValue()
                    .orReturn {
                        yield(it)
                        return@sequence
                    }
                yield(value.toNormal())
                if (done) break
            }
        }
    )
        .toWideNormal()
}
