package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.NumberType
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.parse.foldElvis
import io.github.andjsrk.v4.tokenize.Token

class NumberLiteralNode(token: Token): DynamicPrimitiveLiteralNode<Double>(token), ObjectLiteralKeyNode {
    @EsSpec("NumericValue")
    override val value by lazy {
        val parsedNonDecimal = arrayOf("0b" to 2, "0o" to 8, "0x" to 16)
            .asSequence()
            .map { (prefix, radix) ->
                val removed = raw.removePrefixOrNull(prefix) ?: return@map null
                removed.toInt(radix).toDouble()
            }
            .foldElvis()
        parsedNonDecimal ?: raw.toDouble()
    }
    override fun evaluate() =
        Completion.normal(NumberType(value))
}

private fun String.removePrefixOrNull(prefix: CharSequence) =
    removePrefix(prefix).takeIf { it !== this }
