package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.languageValue

class SymbolType(val description: StringType? = null): PrimitiveLanguageType, PropertyKey {
    /**
     * Note that symbols will be compared by its identity, not [value].
     */
    override val value = null
    override fun toString() =
        "Symbol(${description?.value ?: ""})"

    object WellKnown {
        @EsSpec("Symbol.iterator")
        val iterator = SymbolType("Symbol.iterator".languageValue)
        val toString = SymbolType("Symbol.toString".languageValue)
    }
    companion object {
        val registry = mutableMapOf<String, SymbolType>()
    }
}
