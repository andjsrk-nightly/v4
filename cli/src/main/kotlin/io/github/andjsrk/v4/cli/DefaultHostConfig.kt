package io.github.andjsrk.v4.cli

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.builtin.Object
import io.github.andjsrk.v4.evaluate.orReturn
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

open class DefaultHostConfig: HostConfig() {
    override fun loadImportedModule(module: CyclicModule, specifier: String, state: GraphLoadingState): EmptyOrAbrupt {
        val path = Path(moduleAbsolutePaths[module]!!).join(specifier)
        val (actualPath, sourceText) = path.resolveAndRead()
            .orReturn { return it }
            .value
        val loadedModule = parseModuleOrExit(sourceText, module.realm)
        moduleAbsolutePaths[loadedModule] = actualPath.absolutePathString()
        loadedModule.initializeEnvironment()
            .orReturn { return it }
        module.finishLoadingImportedModule(specifier, state, loadedModule.toWideNormal())
        return empty
    }
    override fun onGotUncaughtAbrupt(abrupt: Completion.Abrupt): Nothing {
        exitWithAbrupt(abrupt)
    }
    override fun applyGlobalProperties(global: ObjectType) {
        for ((key, desc) in globalObj.properties) {
            global.definePropertyOrThrow(key, desc)
        }
    }
    override fun display(value: LanguageType, raw: Boolean): String =
        when (value) {
            NullType -> "null"
            is StringType ->
                if (raw) Json.encodeToString(value.value)
                else value.value
            is NumberType -> value.toString(10).value
            is BigIntType -> value.toString(10).value
            is BooleanType -> value.value.toString()
            is SymbolType -> value.toString()
            is ArrayType -> {
                val mutabilityPrefix = (value is MutableArrayType).thenTake { "(mutable) " }.orEmpty()
                val items = value.array.joinToString(", ") { display(it, true) }
                "$mutabilityPrefix[$items]"
            }
            is ObjectType -> {
                val prefix =
                    if (value.prototype == Object.instancePrototype) ""
                    else {
                        val name = value.prototype?.ownerClass?.name?.let(::display) ?: "(anonymous)"
                        "$name "
                    }
                val props = value.properties.asSequence()
                    .filter { (_, desc) -> desc.enumerable }
                    .map { (k, desc) ->
                        val key =
                            when (k) {
                                is StringType ->
                                    if (k.value.all { it.isIdentifierChar }) k.value
                                    else display(k, true)
                                is SymbolType -> display(k)
                            }
                        val value = when (desc) {
                            is DataProperty -> display(desc.value, true)
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
                var whitespace = ""
                val joinedWithoutNewline = props.joinToString(", ")
                val joined =
                    if (5 < value.properties.size || 80 < joinedWithoutNewline.length) {
                        whitespace = "\n"
                        props.joinToString(",\n") { "  $it" }
                    }
                    else joinedWithoutNewline
                "$prefix{$whitespace$joined$whitespace}"
            }
        }
}

internal fun parseModuleOrExit(sourceText: String, realm: Realm) =
    when (val moduleOrErr = parseModule(sourceText, realm)) {
        is Valid -> moduleOrErr.value
        is Invalid -> exitWithError(moduleOrErr.value)
    }
