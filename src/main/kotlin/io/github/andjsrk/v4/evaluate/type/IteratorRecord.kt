package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.thenTake

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
                        returnMethod.requireToBe<FunctionType> { return it }
                        returnMethod._call(sourceObject, emptyList())
                    } else it
                }
        completion.returnIfAbrupt { return it }
        returnRes
            .returnIfAbrupt { return it }
            .requireToBe<ObjectType> { return it }
        return completion
    }

    companion object {
        @EsSpec("GetIterator")
        fun from(value: LanguageType, kind: GeneratorKind): MaybeAbrupt<IteratorRecord> {
            val method =
                (kind == GeneratorKind.ASYNC).thenTake {
                    value.getMethod(SymbolType.WellKnown.asyncIterator)
                        .returnIfAbrupt { return it }
                }
                    ?: value.getMethod(SymbolType.WellKnown.iterator) // falls back to @@iterator method
                        .returnIfAbrupt { return it }
                    ?: return throwError(TypeErrorKind.NOT_ITERABLE)
            return fromIteratorMethod(value, method)
        }
        @EsSpec("GetIteratorFromMethod")
        fun fromIteratorMethod(value: LanguageType, method: FunctionType): MaybeAbrupt<IteratorRecord> {
            val iterator = method._call(value, emptyList())
                .returnIfAbrupt { return it }
                .requireToBe<ObjectType> { return it }
            val nextMethod = iterator.getMethod("next".languageValue)
                .returnIfAbrupt { return it }
                ?: return throwError(TypeErrorKind.NOT_AN_ITERATOR)
            return IteratorRecord(iterator, nextMethod).toWideNormal()
        }
    }
}

@EsSpec("IteratorComplete")
internal inline fun ObjectType.isDoneIterResult(rtn: AbruptReturnLambda) =
    get("done".languageValue)
        .returnIfAbrupt(rtn)
        .requireToBe<BooleanType>(rtn)
        .value
@EsSpec("IteratorValue")
internal fun ObjectType.extractIterResultValue() =
    get("value".languageValue)
