package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.SyntaxErrorKind
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.json.Json as KotlinxJson

private const val SPACE = '\u0020'

private fun JsonElement.toLanguageValue(): NonEmptyNormalOrAbrupt {
    return when (this) {
        is JsonObject ->
            ObjectType(properties=mutableMapOf(
                *entries.map {
                    val key = it.key.languageValue
                    val value = it.value.toLanguageValue()
                        .orReturn { return it }
                    key to DataProperty(value)
                }
                    .toTypedArray()
            ))
        is JsonArray ->
            ImmutableArrayType(
                map {
                    it.toLanguageValue()
                        .orReturn { return it }
                }
            )
        is JsonNull -> NullType
        is JsonPrimitive -> when {
            isString ->
                // string
                content.languageValue
            content == "true" || content == "false" ->
                // boolean
                content.toBooleanStrict().languageValue
            else -> {
                // number
                if (content.getOrNull(0) == '0' && content.getOrNull(1)?.isDigit() == true) {
                    return throwError(SyntaxErrorKind.JSON_PARSE_NO_LEADING_ZERO)
                }
                content.toDouble().languageValue
            }
        }
    }
        .toNormal()
}
val parse = functionWithoutThis("parse", 1u) fn@ { args ->
    val string = args[0].requireToBeString { return@fn it }
    val decoded = try {
        KotlinxJson.decodeFromString<JsonElement>(string)
    } catch (e: SerializationException) {
        return@fn throwError(SyntaxErrorKind.INVALID_JSON, string)
    }
    decoded.toLanguageValue()
        .orReturn { return@fn it }
        .toNormal()
}

@EsSpec("JSON Serialization Record")
private data class JsonStringifyContext(
    /**
     * Indicates the string that is given by user.
     */
    val indentArg: String,
    val indentAcc: String = "",
    val stack: Stack<ObjectType> = Stack(),
)
private fun validateNonCircularStructure(stack: Stack<ObjectType>, value: ObjectType): EmptyOrAbrupt =
    if (value in stack) throwError(TypeErrorKind.CIRCULAR_STRUCTURE)
    else empty
private fun StringBuilder.appendJsonStringifiedLanguageValue(value: LanguageType, ctx: JsonStringifyContext): EmptyOrAbrupt {
    var value = value
    val toJsonMethod = value.getMethod(SymbolType.WellKnown.toJson)
        .orReturn { return it }
    if (toJsonMethod != null) {
        value = toJsonMethod.call(value, listOf(ctx.indentArg.languageValue))
            .orReturn { return it }
    }
    val needIndent = ctx.indentArg.isNotEmpty()
    val newIndentAcc = ctx.indentAcc + ctx.indentArg
    val oldIndent = needIndent.thenTake { "\n${ctx.indentAcc}" }.orEmpty()
    val newIndent = needIndent.thenTake { "\n$newIndentAcc" }.orEmpty()
    when (value) {
        NullType -> append("null")
        is StringType -> append(KotlinxJson.encodeToString(value.value))
        is BooleanType -> append(value.toString())
        is NumberType -> append(value.toString(10).value)
        is BigIntType -> return throwError(TypeErrorKind.CANNOT_CONVERT_BIGINT_TO_JSON)
        is SymbolType -> return throwError(TypeErrorKind.CANNOT_CONVERT_SYMBOL_TO_JSON)
        is FunctionType -> return throwError(TypeErrorKind.CANNOT_CONVERT_FUNCTION_TO_JSON)
        is ArrayType -> {
            validateNonCircularStructure(ctx.stack, value)
                .orReturn { return it }
            append('[')
            ctx.stack.addTop(value) // stack will be shared with newCtx since both are same instance
            val newCtx = ctx.copy(indentAcc=newIndentAcc)
            for ((i, elem) in value.array.withIndex()) {
                append(newIndent)
                appendJsonStringifiedLanguageValue(elem, newCtx)
                if (i != value.array.lastIndex) append(',')
            }
            ctx.stack.removeTop()
            append(oldIndent)
            append(']')
        }
        is ObjectType -> {
            validateNonCircularStructure(ctx.stack, value)
                .orReturn { return it }
            append('{')
            ctx.stack.addTop(value)
            val newCtx = ctx.copy(indentAcc=newIndentAcc)
            val keys = value.ownEnumerableStringPropertyKeys()
            val comma = if (needIndent) ": " else ":"
            for ((i, key) in keys.withIndex()) {
                append(newIndent)
                appendJsonStringifiedLanguageValue(key, ctx)
                append(comma)
                val value = value.get(key)
                    .orReturn { return it }
                appendJsonStringifiedLanguageValue(value, newCtx)
                if (i != keys.lastIndex) append(',')
            }
            ctx.stack.removeTop()
            append(oldIndent)
            append('}')
        }
    }
    return empty
}
val stringify = functionWithoutThis("stringify", 1u) fn@ { args ->
    val value = args[0]
    val nonNormalizedIndentArg = args.getOrNull(1)
    val indent = when (val indentArg = nonNormalizedIndentArg?.normalizeNull()) {
        null -> ""
        is StringType -> indentArg.value
        is NumberType -> {
            val repeatCount = indentArg.requireToBeUnsignedInt { return@fn it }
            SPACE.toString().repeat(repeatCount)
        }
        else -> return@fn unexpectedType(indentArg, StringType::class, NumberType::class)
    }
    val string = StringBuilder()
    string.appendJsonStringifiedLanguageValue(value, JsonStringifyContext(indent))
        .orReturn { return@fn it }
    string.toString()
        .languageValue
        .toNormal()
}

val Json = ObjectType(properties=mutableMapOf(
    sealedMethod(parse),
    sealedMethod(stringify),
))
