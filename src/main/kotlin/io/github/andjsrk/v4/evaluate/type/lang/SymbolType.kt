package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.languageValue

class SymbolType(val description: StringType? = null): PrimitiveLanguageType, PropertyKey {
    /**
     * Note that symbols will be compared by its identity, not [value].
     */
    override val value = null

    object WellKnown {
        val iterator = SymbolType("Symbol.iterator".languageValue)
        val toString = SymbolType("Symbol.toString".languageValue)
    }
}
