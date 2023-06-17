package io.github.andjsrk.v4.evaluate.type.spec

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

@EsSpec("data property")
data class DataProperty(
    var value: LanguageType,
    var writable: Boolean = true,
    override var enumerable: Boolean = true,
    override var configurable: Boolean = true,
): Property {
    override fun clone() = copy()
}
