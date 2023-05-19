package io.github.andjsrk.v4.tokenize

enum class SyntacticPair(val openingPart: String, val closingPart: String) {
    TEMPLATE_LITERAL("\${", "}");

    companion object {
        fun findByOpeningPart(openingPart: String) =
            values().find { it.openingPart == openingPart }
    }
}
