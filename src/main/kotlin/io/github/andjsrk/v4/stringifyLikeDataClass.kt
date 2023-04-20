package io.github.andjsrk.v4

import io.github.andjsrk.v4.parse.node.Node
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KProperty0

// if this is declared as an extension function,
// we do not need to pass type of this explicitly because it will be inferred
internal inline fun <reified T: Any> T.stringifyLikeDataClass(vararg properties: KProperty0<*>, pretty: Boolean = true): String =
    stringify(*properties, pretty=pretty, className=className)

private fun stringify(vararg properties: KProperty0<*>, pretty: Boolean, className: String): String =
    if (pretty) {
        if (properties.all {
            val value = it.get()
            value !is Node && value !is Collection<*>
        }) stringify(*properties, pretty=false, className=className)
        else "$className(\n${
            properties
                .map(stringifyProperty { value ->
                    when (value) {
                        is List<*> -> "[\n${value.joinToString(",\n").indentEachLines()}\n]"
                        is String -> Json.encodeToString(value)
                        else -> value.toString()
                    }
                })
                .joinToString(",\n")
                .indentEachLines()
        }\n)"
    } else {
        "$className(${
            properties
                .map(stringifyProperty { it.toString() })
                .joinToString(", ")
        })"
    }

private inline fun stringifyProperty(crossinline valueTransform: (Any?) -> String): (KProperty0<*>) -> String =
    { property ->
        "${property.name}=${valueTransform(property.get())}"
    }

private fun String.indentEachLines() =
    lines().joinToString("\n") { "  $it" }
