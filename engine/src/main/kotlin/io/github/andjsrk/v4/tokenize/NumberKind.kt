package io.github.andjsrk.v4.tokenize

internal enum class NumberKind(val kindSpecifier: Char?) {
    BINARY('b'),
    OCTAL('o'),
    HEX('x'),
    DECIMAL(null);

    companion object {
        fun getByKindSpecifier(specifier: Char?) =
            values().find { it.kindSpecifier == specifier }
    }
}
