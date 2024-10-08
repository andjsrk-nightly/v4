package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.SyntaxErrorKind
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.json.Json as KotlinxJson

private const val SPACE = '\u0020'

private fun JsonElement.toLanguageValue(): NonEmptyOrThrow {
    return when (this) {
        is JsonObject ->
            ObjectType.Impl(mutableMapOf(
                *entries.map {
                    val key = it.key.languageValue
                    val value = it.value.toLanguageValue()
                        .orReturnThrow { return it }
                    key to DataProperty(value)
                }
                    .toTypedArray()
            ))
        is JsonArray ->
            ImmutableArrayType(
                map {
                    it.toLanguageValue()
                        .orReturnThrow { return it }
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
@EsSpec("JSON.parse")
private val parse = functionWithoutThis("parse", 1u) fn@ { args ->
    val string = args[0].requireToBeString { return@fn it }
    val decoded = try {
        KotlinxJson.decodeFromString<JsonElement>(string)
    } catch (e: SerializationException) {
        return@fn throwError(SyntaxErrorKind.INVALID_JSON, string)
    }
    decoded.toLanguageValue()
        .orReturnThrow { return@fn it }
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
private fun validateNonCircularStructure(stack: Stack<ObjectType>, value: ObjectType): EmptyOrThrow =
    if (value in stack) throwError(TypeErrorKind.CIRCULAR_STRUCTURE)
    else empty
private fun StringBuilder.appendJsonStringifiedLanguageValue(value: LanguageType, ctx: JsonStringifyContext): EmptyOrThrow {
    var value = value
    val toJsonMethod = value.getMethod(SymbolType.WellKnown.toJson)
        .orReturnThrow { return it }
    if (toJsonMethod != null) {
        value = toJsonMethod.call(value, listOf(ctx.indentArg.languageValue))
            .orReturnThrow { return it }
    }
    val needIndent = ctx.indentArg.isNotEmpty()
    val newIndentAcc = ctx.indentAcc + ctx.indentArg
    val oldIndent = needIndent.thenTake { "\n${ctx.indentAcc}" }.orEmpty()
    val newIndent = needIndent.thenTake { "\n$newIndentAcc" }.orEmpty()
    when (value) {
        NullType -> append("null")
        is StringType -> append(KotlinxJson.encodeToString(value.nativeValue))
        is BooleanType -> append(value.toString())
        is NumberType -> append(value.toString(10).nativeValue)
        is BigIntType -> return throwError(TypeErrorKind.CANNOT_CONVERT_BIGINT_TO_JSON)
        is SymbolType -> return throwError(TypeErrorKind.CANNOT_CONVERT_SYMBOL_TO_JSON)
        is FunctionType -> return throwError(TypeErrorKind.CANNOT_CONVERT_FUNCTION_TO_JSON)
        is ArrayType -> {
            validateNonCircularStructure(ctx.stack, value)
                .orReturnThrow { return it }
            append('[')
            ctx.stack.addTop(value) // stack will be shared with newCtx since both are same instance
            val newCtx = ctx.copy(indentAcc = newIndentAcc)
            for ((i, elem) in value.array.withIndex()) {
                append(newIndent)
                appendJsonStringifiedLanguageValue(elem, newCtx)
                    .orReturnThrow { return it }
                if (i != value.array.lastIndex) append(',')
            }
            ctx.stack.removeTop()
            append(oldIndent)
            append(']')
        }
        is ObjectType -> {
            validateNonCircularStructure(ctx.stack, value)
                .orReturnThrow { return it }
            append('{')
            ctx.stack.addTop(value)
            val newCtx = ctx.copy(indentAcc = newIndentAcc)
            val comma = if (needIndent) ": " else ":"
            val keys = value.ownEnumerableStringPropertyKeys()
            for ((i, key) in keys.withIndex()) {
                append(newIndent)
                appendJsonStringifiedLanguageValue(key, ctx)
                    .orReturnThrow { return it }
                append(comma)
                val value = value.get(key)
                    .orReturnThrow { return it }
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
@EsSpec("JSON.stringify")
private val stringify = functionWithoutThis("stringify", 1u) fn@ { args ->
    val value = args[0]
    val nonNormalizedIndentArg = args.getOrNull(1)
    val indent = when (val indentArg = nonNormalizedIndentArg?.normalizeNull()) {
        null -> ""
        is StringType -> indentArg.nativeValue
        is NumberType -> {
            val repeatCount = indentArg.requireToBeUnsignedInt { return@fn it }
            SPACE.toString().repeat(repeatCount)
        }
        else -> return@fn unexpectedType(indentArg, StringType::class, NumberType::class)
    }
    val builder = StringBuilder()
    builder.appendJsonStringifiedLanguageValue(value, JsonStringifyContext(indent))
        .orReturnThrow { return@fn it }
    builder.toString()
        .languageValue
        .toNormal()
}

@EsSpec("%JSON%")
val Json = ObjectType.Impl(mutableMapOf(
    sealedMethod(parse),
    sealedMethod(stringify),
))
