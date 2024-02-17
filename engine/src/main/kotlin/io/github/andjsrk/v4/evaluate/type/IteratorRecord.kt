package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*

class IteratorRecord(
    val sourceObject: ObjectType, // [[Iterator]]
    val nextMethod: FunctionType,
    done: Boolean = false,
): Record {
    var done: Boolean = done
        private set
    @EsSpec("IteratorNext")
    fun next(value: LanguageType? = null): MaybeThrow<IteratorResult> {
        return nextMethod.call(sourceObject, if (value == null) emptyList() else listOf(value))
            .orReturnThrow { return it }
            .requireToBe<ObjectType> { return it }
            .asIteratorResult()
            .toWideNormal()
    }
    @EsSpec("IteratorStep")
    fun step(): MaybeThrow<IteratorResult?> {
        val rawRes = next()
        val res = rawRes.orReturnThrow { return it }
        val done = res.getDone()
            .orReturnThrow { return it }
            .value
        return if (done) empty else rawRes
    }
    @EsSpec("IteratorClose")
    fun <V: AbstractType?> close(completion: Completion.FromFunctionBody<V>): Completion.FromFunctionBody<V> {
        val res =
            sourceObject.getMethod("close".languageValue)
                .let {
                    if (it is Completion.Normal) {
                        val closeMethod = it.value ?: return completion
                        closeMethod.call(sourceObject)
                    } else it
                }
        if (completion is Completion.Throw) return completion
        res.orReturnThrow { return it } // just ignore, do not require anything to the language value (the original requires it to be an object)
        return completion
    }
    fun toSequence(): Sequence<NonEmptyOrThrow> =
        sequence {
            while (true) {
                val value = step()
                    .orReturnThrow {
                        yield(close(it) as NonEmptyOrThrow)
                        return@sequence
                    }
                    ?: break
                yield(value.getValue())
            }
            close(empty)
        }

    companion object {
        @EsSpec("GetIterator")
        fun from(value: LanguageType): MaybeThrow<IteratorRecord> {
            val method =
                value.getMethod(SymbolType.WellKnown.iterator)
                    .orReturnThrow { return it }
                    ?: return throwError(TypeErrorKind.NOT_ITERABLE)
            return fromIteratorMethod(value, method)
        }
        @EsSpec("GetIteratorFromMethod")
        fun fromIteratorMethod(value: LanguageType, method: FunctionType): MaybeThrow<IteratorRecord> {
            val iterator = method.call(value)
                .orReturnThrow { return it }
                .requireToBe<ObjectType> { return it }
            val nextMethod = iterator.getMethod("next".languageValue)
                .orReturnThrow { return it }
                ?: return throwError(TypeErrorKind.NOT_AN_ITERATOR)
            return IteratorRecord(iterator, nextMethod)
                .toWideNormal()
        }
    }
}
