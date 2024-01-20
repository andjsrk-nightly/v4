package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*

class IteratorRecord(
    val sourceObject: ObjectType, // [[Iterator]]
    val nextMethod: FunctionType,
    done: Boolean = false,
): Record {
    var done: Boolean = done
        private set
    @EsSpec("IteratorNext")
    fun next(value: LanguageType? = null): MaybeAbrupt<IteratorResult> {
        return nextMethod.call(sourceObject, if (value == null) emptyList() else listOf(value))
            .orReturn { return it }
            .requireToBe<ObjectType> { return it }
            .asIteratorResult()
            .toWideNormal()
    }
    @EsSpec("IteratorStep")
    fun step(): MaybeAbrupt<IteratorResult?> {
        val rawRes = next()
        val res = rawRes.orReturn { return it }
        val done = res.getDone()
            .orReturn { return it }
            .value
        return if (done) empty else rawRes
    }
    @EsSpec("IteratorClose")
    fun <V: AbstractType?> close(completion: MaybeAbrupt<V>): MaybeAbrupt<V> {
        val res =
            sourceObject.getMethod("close".languageValue)
                .let {
                    if (it is Completion.Normal) {
                        val closeMethod = it.value ?: return completion
                        closeMethod.call(sourceObject)
                    } else it
                }
        completion.orReturn { return it }
        res.orReturn { return it } // just ignore, do not require anything to the language value (the original requires it to be an object)
        return completion
    }
    fun toSequence(): Sequence<NonEmptyOrAbrupt> =
        sequence {
            while (true) {
                val value = step()
                    .orReturn {
                        yield(close(it))
                        return@sequence
                    }
                    ?: break
                yield(value.getValue())
            }
            close(empty)
        }

    companion object {
        @EsSpec("GetIterator")
        fun from(value: LanguageType): MaybeAbrupt<IteratorRecord> {
            val method =
                value.getMethod(SymbolType.WellKnown.iterator)
                    .orReturn { return it }
                    ?: return throwError(TypeErrorKind.NOT_ITERABLE)
            return fromIteratorMethod(value, method)
        }
        @EsSpec("GetIteratorFromMethod")
        fun fromIteratorMethod(value: LanguageType, method: FunctionType): MaybeAbrupt<IteratorRecord> {
            val iterator = method.call(value)
                .orReturn { return it }
                .requireToBe<ObjectType> { return it }
            val nextMethod = iterator.getMethod("next".languageValue)
                .orReturn { return it }
                ?: return throwError(TypeErrorKind.NOT_AN_ITERATOR)
            return IteratorRecord(iterator, nextMethod)
                .toWideNormal()
        }
    }
}
