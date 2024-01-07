package io.github.andjsrk.v4.parse

import io.github.andjsrk.v4.parse.node.Node
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KProperty0

// if this is declared as an extension function,
// we do not need to pass type of this explicitly because it will be inferred
internal inline fun <T: Node> T.stringifyLikeDataClass(vararg properties: KProperty0<*>, pretty: Boolean = true): String =
    stringify(*properties, pretty=pretty, className=className)

private fun stringify(vararg properties: KProperty0<*>, pretty: Boolean, className: String): String =
    if (pretty) {
        // if all properties are neither Node nor Collection, return same result as pretty=false
        if (properties.all {
            val value = it.get()
            value !is Node && value !is Collection<*>
        }) stringify(*properties, pretty=false, className=className)
        else "$className(\n${
            properties
                .joinToString(",\n", transform=stringifyProperty { value ->
                    when (value) {
                        is List<*> -> "[\n${value.joinToString(",\n").indentEachLines()}\n]"
                        is String -> Json.encodeToString(value)
                        else -> value.toString()
                    }
                })
                .indentEachLines()
        }\n)"
    } else {
        "$className(${
            properties.joinToString(", ", transform=stringifyProperty { it.toString() })
        })"
    }

private inline fun stringifyProperty(crossinline valueTransform: (Any?) -> String): (KProperty0<*>) -> String =
    { property ->
        "${property.name}=${valueTransform(property.get())}"
    }

private fun String.indentEachLines() =
    lines().joinToString("\n") { "  $it" }

private inline val Any.className get() =
    this::class.simpleName!!
