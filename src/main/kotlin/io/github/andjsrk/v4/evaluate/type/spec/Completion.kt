package io.github.andjsrk.v4.evaluate.type.spec

import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

data class Completion(val type: Type, val value: AbstractType): Record {
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
        inline fun wideNormal(value: AbstractType) =
            Completion(Type.NORMAL, value)
        inline fun normal(value: LanguageType) =
            wideNormal(value)
    }
}
