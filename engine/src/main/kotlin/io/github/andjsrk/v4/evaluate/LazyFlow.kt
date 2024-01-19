package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.toIterableIterator
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*
import kotlin.experimental.ExperimentalTypeInference

private typealias Input = NonEmptyOrAbrupt
private typealias Yield = NonEmptyOrAbrupt

typealias SimpleLazyFlow<R> = LazyFlow<R, R>

/**
 * An approximate implementation of `function*` syntax in ES.
 *
 * Note that [C] does not support nullable types for simple implementation
 * since it is not needed yet.
 *
 * @see SequenceBuilderIterator
 */
class LazyFlow<out R: C, out C: MaybeAbrupt<*>>(block: suspend LazyFlow<R, C>.() -> R): Iterator<C> {
    var isDone = false
    private var nextCallInput: Input? = null
    private var yieldedValue: Yield? = null
    var returnValue: @UnsafeVariance R? = null
    private var nextStep: Continuation<Unit>? =
        block.createCoroutineUnintercepted(
            this,
            object: Continuation<R> {
                override fun resumeWith(result: Result<@UnsafeVariance R>) {
                    returnValue = result.getOrThrow()
                    nextCallInput = null
                    isDone = true
                }
                override val context = EmptyCoroutineContext
            },
        )

    /**
     * WARNING: The iterator does not produce values
     * that the [LazyFlow] produced from [next] call previously.
     * This means the code below outputs `[2]`, not `[1, 2]`:
     * ```kt
     * val a = LazyFlow {
     *     yield(1)
     *     yield(2)
     *     0
     * }
     * a.next()
     * println(a.yieldedValues.toList())
     * ```
     */
    val yieldedValues by lazy {
        iterator {
            while (true) {
                val value = next()
                if (isDone) break // ignore return value
                yield(value as Yield)
            }
        }
            .toIterableIterator()
    }

    override fun hasNext() =
        !isDone
    fun next(value: Input?): C {
        nextCallInput = value
        return next()
    }
    override fun next(): C {
        if (isDone) throw NoSuchElementException()
        val step = nextStep!!
        nextStep = null
        step.resume(Unit)
        if (isDone) return returnValue ?: throw NoSuchElementException("A LazyFlow must return something, but it did not.")
        else return yieldedValue!! as C
    }
    suspend fun yield(value: Yield): Input? {
        yieldedValue = value
        suspendCoroutineUninterceptedOrReturn { c ->
            nextStep = c
            COROUTINE_SUSPENDED
        }
        val input = nextCallInput
        nextCallInput = null
        return input
    }
    suspend fun <R2: MaybeAbrupt<*>> yieldAll(other: LazyFlow<R2, *>): R2 {
        other.yieldedValues.forEach { yield(it) }
        return other.returnValue!!
    }
    suspend fun yieldAll(iterable: Iterable<Yield>) {
        iterable.forEach { yield(it) }
    }

    /**
     * Drops all values that the [LazyFlow] yields and then returns the return value.
     */
    fun unwrap(): R {
        while (!isDone) next()
        return returnValue!!
    }
}

@OptIn(ExperimentalTypeInference::class)
fun <R: YRC, YRC: MaybeAbrupt<*>> lazyFlow(@BuilderInference block: suspend LazyFlow<R, YRC>.() -> R) =
    LazyFlow(block)

@OptIn(ExperimentalTypeInference::class)
fun <R: MaybeAbrupt<*>> lazyFlowNoYields(@BuilderInference block: suspend SimpleLazyFlow<R>.() -> R) =
    SimpleLazyFlow(block)
