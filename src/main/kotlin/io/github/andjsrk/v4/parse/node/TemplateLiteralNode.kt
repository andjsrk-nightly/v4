package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.toNormal
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class TemplateLiteralNode(
    val strings: List<TemplateStringNode>,
    val expressions: List<ExpressionNode>,
): ExpressionNode, NonAtomicNode {
    override val childNodes get() = strings.flatZip(expressions)
    override val range = strings.first().range..strings.last().range
    override fun toString() =
        stringifyLikeDataClass(::strings, ::expressions, ::range)
    override fun evaluate() =
        EvalFlow {
            val result = StringBuilder(strings[0].value)
            var i = 0
            for (string in strings) {
                if (i == 0) {
                    // the first string is already taken, so skip it
                    i++
                    continue
                }
                val value = expressions[i].evaluateValue()
                    .returnIfAbrupt(this) { return@EvalFlow }
                val stringValue = stringify(value)
                result.append(stringValue)
                result.append(string)
                i++
            }
            `return`(result.toString().languageValue.toNormal())
        }
}

/**
 * Returns a list that has alternately element of this collection and element of the other array with the same index.
 * The returned list has length as long as possible.
 *
 * @see List.zip
 */
private fun <T> List<T>.flatZip(other: Iterable<T>) =
    sequence {
        val firstIt = iterator()
        val secondIt = other.iterator()
        while (firstIt.hasNext() && secondIt.hasNext()) {
            yield(firstIt.next())
            yield(secondIt.next())
        }
        yieldAll(firstIt)
        yieldAll(secondIt)
    }
        .toList()
