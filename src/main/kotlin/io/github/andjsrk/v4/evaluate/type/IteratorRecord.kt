package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*

data class IteratorRecord(
    val sourceObject: ObjectType, // [[Iterator]]
    val nextMethod: FunctionType,
    var done: Boolean = false,
): Record {
    @EsSpec("IteratorClose")
    fun close(completion: NormalOrAbrupt): NormalOrAbrupt {
        val returnRes =
            sourceObject.getMethod("return".languageValue)
                .let {
                    if (it is Completion.Normal) {
                        val returnMethod = it.value?.normalizeNull() ?: return completion
                        returnMethod.call(sourceObject, emptyList())
                    } else it
                }
        completion.orReturn { return it }
        returnRes
            .orReturn { return it }
            .requireToBe<ObjectType> { return it }
        return completion
    }

    companion object {
        // @EsSpec("GetIterator")
        // fun from(value: LanguageType, kind: GeneratorKind): MaybeAbrupt<IteratorRecord> {
        //     val method =
        //         (kind == GeneratorKind.ASYNC).thenTake {
        //             value.getMethod(SymbolType.WellKnown.asyncIterator)
        //                 .orReturn { return it }
        //         }
        //             ?: value.getMethod(SymbolType.WellKnown.iterator)
        //                 .orReturn { return it }
        //             ?: return throwError(TypeErrorKind.NOT_ITERABLE)
        //     return fromIteratorMethod(value, method)
        // }
        @EsSpec("GetIteratorFromMethod")
        fun fromIteratorMethod(value: LanguageType, method: FunctionType): MaybeAbrupt<IteratorRecord> {
            val iterator = method.call(value, emptyList())
                .orReturn { return it }
                .requireToBe<ObjectType> { return it }
            val nextMethod = iterator.getMethod("next".languageValue)
                .orReturn { return it }
                ?: return throwError(TypeErrorKind.NOT_AN_ITERATOR)
            return IteratorRecord(iterator, nextMethod).toWideNormal()
        }
    }
}

@EsSpec("IteratorComplete")
internal fun ObjectType.getIterResultDone(): MaybeAbrupt<BooleanType> {
    return get("done".languageValue)
        .orReturn { return it }
        .requireToBe<BooleanType> { return it }
        .toNormal()
}

@EsSpec("IteratorValue")
internal fun ObjectType.getIterResultValue() =
    get("value".languageValue)
