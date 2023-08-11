package io.github.andjsrk.v4

import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * An approximate implementation of `function*` syntax in ES.
 *
 * @see SequenceBuilderIterator
 */
open class EsGenerator<I, Y, out R: Any>: Iterator<Y>, Continuation<Unit> {
    /**
     * @see kotlin.sequences.State
     */
    private enum class State {
        NOT_READY,
        MANY_NOT_READY,
        READY,
        MANY_READY,
        DONE,
        FAILED,
    }

    private var state = State.NOT_READY
    private var nextValue: Y? = null
    private var nextIter: Iterator<Y>? = null
    private var nextCallInput: I? = null
    private var returnValue: @UnsafeVariance R? = null
    var nextStep: Continuation<Unit>? = null

    override fun hasNext(): Boolean {
        while (true) {
            when (state) {
                State.NOT_READY -> {}
                State.MANY_NOT_READY ->
                    if (nextIter!!.hasNext()) {
                        state = State.MANY_READY
                        return true
                    } else {
                        nextIter = null
                    }
                State.DONE -> return false
                State.READY, State.MANY_READY -> return true
                else -> throw exceptionalState()
            }
            state = State.FAILED
            val step = nextStep!!
            nextStep = null
            step.resume(Unit)
        }
    }
    fun next(value: I): Y {
        nextCallInput = value
        return next()
    }
    override fun next(): Y {
        return when (state) {
            State.NOT_READY, State.MANY_NOT_READY ->
                if (!hasNext()) throw NoSuchElementException()
                else next()
            State.MANY_READY -> {
                state = State.MANY_NOT_READY
                nextIter!!.next()
            }
            State.READY -> {
                state = State.NOT_READY
                val result = nextValue!!
                nextValue = null
                result
            }
            else -> throw exceptionalState()
        }
    }
    fun nextIfPresent(value: I? = null): Y? =
        hasNext().thenTake {
            if (value != null) next(value)
            else next()
        }
    private fun exceptionalState(): Exception =
        when (state) {
            State.DONE -> NoSuchElementException()
            State.FAILED -> IllegalStateException("Iterator has failed.")
            else -> neverHappens()
        }

    /**
     * @see SequenceBuilderIterator.yield
     */
    suspend fun yield(value: Y): I? {
        nextValue = value
        state = State.READY
        suspendCoroutineUninterceptedOrReturn { c ->
            nextStep = c
            COROUTINE_SUSPENDED
        }
        val input = nextCallInput
        nextCallInput = null
        return input
    }
    /**
     * @return Returned value from [gen]
     * @see SequenceBuilderIterator.yieldAll
     */
    suspend fun <R2: Any> yieldAll(gen: EsGenerator<I, Y, R2>): R2? {
        yieldAll(gen.iterator())
        @Suppress("INVISIBLE_MEMBER") // suppress the error since the access can never fail
        return gen.returnValue
    }
    /**
     * @see SequenceBuilderIterator.yieldAll
     */
    suspend fun yieldAll(iterator: Iterator<Y>) {
        nextIter = iterator
        state =
            if (iterator.hasNext()) State.MANY_READY
            else State.MANY_NOT_READY
        suspendCoroutineUninterceptedOrReturn { c ->
            nextStep = c
            COROUTINE_SUSPENDED
        }
    }
    suspend fun `return`(value: @UnsafeVariance R?): Nothing {
        returnValue = value
        state = State.DONE
        suspendCoroutineUninterceptedOrReturn<Unit> { _ ->
            nextStep = null
            COROUTINE_SUSPENDED
        }
        neverHappens()
    }
    val canTakeReturnValue get() =
        state == State.DONE
    fun takeReturnValue(): R? {
        val isNextStepPresent = nextStep != null
        nextStep?.resume(Unit)
        if (state != State.DONE && isNextStepPresent) throw IllegalStateException("The generator did not encountered end of block yet.")
        return returnValue
    }

    override fun resumeWith(result: Result<Unit>) {
        result.getOrThrow() // just rethrow exception if it is there
        state = State.DONE
    }
    override val context: CoroutineContext
        get() = EmptyCoroutineContext
}
