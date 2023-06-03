package io.github.andjsrk.v4.evaluate.type.spec

import io.github.andjsrk.v4.evaluate.type.AbstractType

data class Completion(val type: Type, val value: AbstractType): Record {
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
        fun normal(value: AbstractType) =
            Completion(Type.NORMAL, value)
    }
}
