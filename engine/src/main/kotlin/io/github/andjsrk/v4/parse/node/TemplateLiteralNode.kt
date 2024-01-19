package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.toNormal
import io.github.andjsrk.v4.parse.stringifyLikeDataClass
import io.github.andjsrk.v4.subList

class TemplateLiteralNode(
    val strings: List<TemplateStringNode>,
    val expressions: List<ExpressionNode>,
): ExpressionNode, NonAtomicNode {
    override val childNodes get() = strings.flatZip(expressions)
    override val range = strings.first().range..strings.last().range
    override fun toString() =
        stringifyLikeDataClass(::strings, ::expressions, ::range)
    override fun evaluate() = lazyFlow f@ {
        val result = StringBuilder(strings[0].value)
        strings.subList(1).forEachIndexed { i, string ->
            val value = yieldAll(expressions[i].evaluateValue())
                .orReturn { return@f it }
            val stringified = stringify(value)
                .orReturn { return@f it }
            result.append(stringified.value)
            result.append(string.value)
        }
        result.toString()
            .languageValue
            .toNormal()
    }
}

/**
 * Returns a list that has alternately element of this collection and element of the other list with the same index.
 * The returned list has length as long as possible.
 *
 * @see List.zip
 */
private fun <T> List<T>.flatZip(other: List<T>) =
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
