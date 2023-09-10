package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec

class SymbolType(val description: String? = null): PrimitiveLanguageType, PropertyKey {
    /**
     * Note that symbols will be compared by its identity, not [value].
     */
    override val value = null
    override fun toString() =
        "Symbol(${description ?: ""})"

    object WellKnown {
        @EsSpec("@@search")
        val findMatchedIndex = SymbolType("Symbol.findMatchedIndex")
        @EsSpec("@@iterator")
        val iterator = SymbolType("Symbol.iterator")
        @EsSpec("@@match")
        val match = SymbolType("Symbol.match")
        @EsSpec("@@replace")
        val replace = SymbolType("Symbol.replace")
        @EsSpec("@@split")
        val split = SymbolType("Symbol.split")
        val toJson = SymbolType("Symbol.toJson")
        val toString = SymbolType("Symbol.toString")
    }
    companion object {
        val registry = mutableMapOf<String, SymbolType>()
    }
}
