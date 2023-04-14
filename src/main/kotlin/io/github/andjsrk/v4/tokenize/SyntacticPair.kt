package io.github.andjsrk.v4.tokenize

enum class SyntacticPair(val openingPart: String, val closingPart: String) {
    PARENTHESIS("(", ")"),
    BRACE("{", "}"),
    BRACKET("[", "]"),
    TEMPLATE_LITERAL("\${", "}");
    // no quotes; because they will be processed by `getStringToken` and `getTemplateHeadToken`

    companion object {
        fun findByOpeningPart(openingPart: String) =
            values().find { it.openingPart == openingPart }
    }
}
