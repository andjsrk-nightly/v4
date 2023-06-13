package io.github.andjsrk.v4.evaluate.type.spec

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

@EsSpec("Completion Record")
data class Completion(
    val type: Type,
    val value: AbstractType?,
): Record {
    val languageValue get() =
        value as LanguageType?
    val isNormal get() =
        type == Type.NORMAL
    val isAbrupt get() =
        !isNormal

    fun map(transform: (AbstractType?) -> AbstractType?) =
        if (this.isNormal) copy(value=transform(value))
        else this

    enum class Type {
        NORMAL,
        BREAK,
        CONTINUE,
        RETURN,
        THROW
    }
    companion object {
        /**
         * Returns a normal completion containing a [AbstractType] which is wider than [LanguageType].
         */
        inline fun wideNormal(value: AbstractType?) =
            Completion(Type.NORMAL, value)
        inline fun normal(value: LanguageType) =
            wideNormal(value)
        inline fun `throw`(value: LanguageType) =
            Completion(Type.THROW, value)
        /**
         * Indicates a normal completion containing `empty`.
         */
        val empty by lazy {
            wideNormal(null)
        }
    }
}
