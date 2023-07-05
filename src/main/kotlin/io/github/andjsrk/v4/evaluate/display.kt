package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.type.AccessorProperty
import io.github.andjsrk.v4.evaluate.type.DataProperty
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.isIdentifierChar
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun LanguageType.display(raw: Boolean = false): String =
    when (this) {
        NullType -> "null"
        is StringType ->
            if (raw) Json.encodeToString(value)
            else value
        is NumberType -> this.toString(10).value
        is BigIntType -> value.toString(10)
        is BooleanType -> value.toString()
        is SymbolType -> toString()
        is ObjectType -> {
            val prefix =
                if (prototype == Object.instancePrototype) ""
                else {
                    val name = prototype?.ownerClass?.name?.display() ?: "(anonymous)"
                    "$name "
                }
            val props = properties.asSequence()
                .filter { (_, desc) -> desc.enumerable }
                .map { (k, desc) ->
                    val key =
                        when (k) {
                            is StringType ->
                                if (k.value.all { it.isIdentifierChar }) k.value
                                else k.display(raw=true)
                            is SymbolType -> k.display()
                        }
                    val value = when (desc) {
                        is DataProperty -> desc.value.display(raw=true)
                        is AccessorProperty ->
                            "<${
                                listOfNotNull(
                                    desc.get?.let { "getter" },
                                    desc.set?.let { "setter" },
                                )
                                    .joinToString("/")
                            }>"
                    }
                    "$key: $value"
                }
            var whitespace = " "
            val joinedWithoutNewline = props.joinToString(", ")
            val joined =
                if (5 < properties.size || 80 < joinedWithoutNewline.length) {
                    whitespace = "\n"
                    props.joinToString(",\n") { "  $it" }
                }
                else joinedWithoutNewline
            "$prefix{$whitespace$joined$whitespace}"
        }
    }
