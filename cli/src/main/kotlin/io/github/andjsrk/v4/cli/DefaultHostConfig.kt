package io.github.andjsrk.v4.cli

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.Object
import io.github.andjsrk.v4.evaluate.type.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.timerTask
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

private val timer = Timer()
private val scheduledTaskCount = AtomicInteger(0)

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
    override fun wait(ms: Int): PromiseType {
        val capability = PromiseType.Capability.new()
        scheduledTaskCount.incrementAndGet()
        timer.schedule(timerTask {
            capability.resolve.callWithSingleArg(NullType)
                .unwrap()
            runJobs()

            // decrement must always be performed
            val isTimerIdle = scheduledTaskCount.decrementAndGet() == 0
            if (jobQueue.isEmpty() && isTimerIdle) {
                timer.cancel()
                // everything has finished, so releases the timer to exit the process
            }
        }, ms.toLong())
        return capability.promise
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
                if (raw) value.nativeValue
                else Json.encodeToString(value.nativeValue)
            is NumberType -> value.toString(10).nativeValue
            is BigIntType -> value.toString(10).nativeValue
            is BooleanType -> value.nativeValue.toString()
            is SymbolType -> value.toString()
            is ArrayType -> {
                val mutabilityPrefix = (value is MutableArrayType).thenTake { "(mutable) " }.orEmpty()
                val items = value.array.joinToString(", ") { display(it, false) }
                "$mutabilityPrefix[$items]"
            }
            is ObjectType -> {
                val prefix =
                    if (value.prototype == Object.instancePrototype) ""
                    else {
                        val name = value.prototype?.ownerClass?.name?.string() ?: "(anonymous)"
                        "$name "
                    }
                val props = value.properties.asSequence()
                    .filter { (_, desc) -> desc.enumerable }
                    .map { (k, desc) ->
                        val key =
                            when (k) {
                                is StringType ->
                                    if (k.nativeValue.all { it.isIdentifierChar }) k.nativeValue
                                    else display(k, false)
                                is SymbolType -> display(k)
                                is PrivateName -> k.description
                            }
                        val value = when (desc) {
                            is DataProperty -> display(desc.value, false)
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
