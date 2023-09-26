package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.not

class IteratorRecord(
    val sourceObject: ObjectType, // [[Iterator]]
    val nextMethod: FunctionType,
    done: Boolean = false,
): Record {
    var done: Boolean = done
        private set
    /**
     * NOTE: The method may return [empty] to represent there is no remaining element.
     */
    fun next(value: LanguageType? = null): NormalOrAbrupt {
        val res = nextMethod.call(sourceObject, if (value == null) emptyList() else listOf(value))
            .orReturn { return it }
            .requireToBe<ObjectType> { return it }
            .asIteratorResult()
        val done = res.getDone()
            .orReturn { return it }
            .value
        if (this.not { done }) this.done = done
        if (done) return empty
        return res.getValue()
    }
    @EsSpec("IteratorClose")
    fun <V: AbstractType?> close(completion: MaybeAbrupt<V>): MaybeAbrupt<V> {
        val res =
            sourceObject.getMethod("close".languageValue)
                .let {
                    if (it is Completion.Normal) {
                        val closeMethod = it.value ?: return completion
                        closeMethod.call(sourceObject, emptyList())
                    } else it
                }
        completion.orReturn { return it }
        res.orReturn { return it }
        return completion
    }
    fun toSequence(): Sequence<NonEmptyNormalOrAbrupt> =
        sequence {
            while (true) {
                val value = next()
                    .orReturn {
                        yield(close(it))
                        return@sequence
                    }
                    ?: break
                yield(value.toNormal())
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
            val iterator = method.call(value, emptyList())
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
