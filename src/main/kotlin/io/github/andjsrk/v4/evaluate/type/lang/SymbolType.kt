package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.languageValue

class SymbolType(val description: StringType? = null): PrimitiveLanguageType, PropertyKey {
    internal constructor(description: String?): this(description?.languageValue)
    /**
     * Note that symbols will be compared by its identity, not [value].
     */
    override val value = null
    override fun toString() =
        "Symbol(${description?.value ?: ""})"

    object WellKnown {
        @EsSpec("@@iterator")
        val iterator = SymbolType("Symbol.iterator")
        @EsSpec("@@match")
        val match = SymbolType("Symbol.match")
        @EsSpec("@@replace")
        val replace = SymbolType("Symbol.replace")
        @EsSpec("@@search")
        val findMatchedIndex = SymbolType("Symbol.findMatchedIndex")
        val toString = SymbolType("Symbol.toString")
    }
    companion object {
        val registry = mutableMapOf<String, SymbolType>()
    }
}
