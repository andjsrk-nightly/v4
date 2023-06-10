package io.github.andjsrk.v4.evaluate.type.spec

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NullType

@EsSpec("Completion Record")
data class Completion(
    val type: Type,
    val value: AbstractType,
    /**
     * Note that [value] is not nullable because if it is, operations about [Completion] will be very inconvenient.
     * Instead of, [Completion] uses this property to determine whether the `[[Value]]` is `empty`.
     */
    val isEmpty: Boolean = false,
): Record {
    fun map(transform: (AbstractType) -> AbstractType) =
        if (type.isNormal) copy(value=transform(value))
        else this

    enum class Type {
        NORMAL,
        BREAK,
        CONTINUE,
        RETURN,
        THROW;

        val isNormal get() =
            this == NORMAL
        val isAbrupt get() =
            !isNormal
    }
    companion object {
        /**
         * Returns a normal completion containing a [AbstractType] which is wider than [LanguageType].
         */
        inline fun wideNormal(value: AbstractType) =
            Completion(Type.NORMAL, value)
        inline fun normal(value: LanguageType) =
            wideNormal(value)
        val empty by lazy {
            Completion(Type.NORMAL, NullType, true)
        }
    }
}
