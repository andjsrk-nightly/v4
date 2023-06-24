package io.github.andjsrk.v4

sealed interface MaybeError<out T, out E: Throwable> {
    val value: Any?
}
class Valid<T>(override val value: T): MaybeError<T, Nothing>
class Invalid<E: Throwable>(override val value: E): MaybeError<Nothing, E>
