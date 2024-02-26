package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.display

data object NullType: PrimitiveLanguageType {
    override val nativeValue: Nothing? = null
    override fun toString() = display()
}
