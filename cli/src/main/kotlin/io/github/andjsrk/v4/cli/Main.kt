package io.github.andjsrk.v4.cli

import io.github.andjsrk.v4.Error
import io.github.andjsrk.v4.HostConfig
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.parse.Parser
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val entryPointPath = args.getOrNull(0) ?: return enterReplMode()
    runFile(entryPointPath)
}

val moduleAbsolutePaths = mutableMapOf<Module, String>()

class UncaughtAbruptException: RuntimeException()

fun enterReplMode() {
    val config = object: DefaultHostConfig() {
        override fun onGotUncaughtAbrupt(abrupt: Completion.Abrupt): Nothing {
            eprintAbrupt(abrupt)
            throw UncaughtAbruptException()
        }
    }
    HostConfig.set(config)
    initializeRealm()
    println("Welcome to REPL.")
    while (true) {
        print("> ")
        val line = readlnOrNull() ?: break
        if (line.isEmpty()) continue
        val parser = Parser(line)
        // parses a Module instead of a ModuleItem because the input may be multiple statements
        val module = parser.parseModule()
        if (module == null) {
            eprintln(parser.error?.toErrorObject()?.display())
            continue
        }
        instantiateBlockDeclaration(module, runningExecutionContext.lexicalEnvNotNull)
        try {
            val evalRes = module.evaluate()
            evalRes.yieldedValues.forEach { it.orReturn(config::onGotUncaughtAbrupt) }
            val result = evalRes
                .unwrap()
                .orReturn(config::onGotUncaughtAbrupt)
            println(result.display())
        } catch (_: UncaughtAbruptException) {}
    }
}

private inline fun getCwd() =
    Path.of("")

fun runFile(path: String) {
    val cwd = getCwd()
    val (entryPointPath, entryPointContent) = cwd.resolve(path).let { p ->
        p.resolveAndRead()
            .orReturn { throw NoSuchFileException(p.absolutePathString()) }
            .value
    }
    HostConfig.set(object: DefaultHostConfig() {
        override fun onGotUncaughtAbrupt(abrupt: Completion.Abrupt): Nothing {
            exitWithAbrupt(abrupt)
        }
    })
    initializeRealm()
    val module = parseModuleOrExit(entryPointContent, runningExecutionContext.realm)
    moduleAbsolutePaths[module] = entryPointPath.absolutePathString()
    module.loadRequestedModules()
        .onRejected {
            eprintln("failed to load modules requested by '${moduleAbsolutePaths[module]}': ${it.display()}")
        }
    module.link()
        .orReturn(::exitWithAbrupt)
    module.initializeEnvironment()
        .orReturn(::exitWithAbrupt)
    module.evaluate()
        .onRejected {
            eprintln("failed to evaluate module '${moduleAbsolutePaths[module]}': ${it.display()}")
        }
    runJobs()
}

private fun PromiseType.onRejected(callback: (reason: LanguageType) -> Unit) =
    also {
        then(null, BuiltinFunctionType(requiredParameterCount=1u) { _, (reason) ->
            callback(reason)
            normalNull
        }, PromiseType.Capability.new())
    }

internal fun eprintValue(value: LanguageType) =
    eprintln(value.display())
internal fun eprintAbrupt(abrupt: Completion.Abrupt) =
    eprintValue(abrupt.value!!)
internal fun exitWithAbrupt(abrupt: Completion.Abrupt): Nothing {
    eprintAbrupt(abrupt)
    exitProcess(1)
}
internal fun exitWithError(error: Error): Nothing {
    eprintValue(error.toErrorObject())
    exitProcess(1)
}

internal fun LanguageType.display() =
    HostConfig.value.display(this)
