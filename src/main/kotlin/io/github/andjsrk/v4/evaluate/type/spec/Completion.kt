package io.github.andjsrk.v4.evaluate.type.spec

import io.github.andjsrk.v4.evaluate.type.AbstractType

data class Completion(val type: Type, val value: AbstractType?): Record {
    val isAbrupt get() =
        type != Type.NORMAL

    enum class Type {
        NORMAL,
        BREAK,
        CONTINUE,
        RETURN,
        THROW
    }
    companion object {
        fun normal(value: AbstractType?) =
            Completion(Type.NORMAL, value)
    }
}
