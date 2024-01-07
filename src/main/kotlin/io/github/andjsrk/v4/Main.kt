package io.github.andjsrk.v4

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.normalNull
import io.github.andjsrk.v4.parse.Parser
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory
import kotlin.system.exitProcess

const val languageExtension = ".ss"

fun main(args: Array<String>) {
    val entryPointPath = args.getOrNull(0) ?: return enterReplMode()
    runFile(entryPointPath)
}

fun enterReplMode() {
    println("Welcome to REPL.")
    initializeRealm()
    while (true) {
        print("> ")
        val line = readlnOrNull() ?: break
        if (line.isEmpty()) continue
        val parser = Parser(line)
        // parses a Module instead of a ModuleItem because the input may be multiple statements
        val module = parser.parseModule() ?: return eprintln(parser.error)
        instantiateBlockDeclaration(module, runningExecutionContext.lexicalEnvironment)
        val result = module.evaluate()
            .orReturn(::exitWithThrow)
        println(result.display())
    }
}

private inline fun getCwd() =
    Path.of("")

fun runFile(path: String) {
    val cwd = getCwd()
    val (entryPointPath, entryPointContent) = cwd.resolve(path).let {
        if (it.isDirectory()) TODO("Treating a directory as an entry point is not implemented yet")
        else it.readTextThenPair()
            ?: cwd.resolve("$path$languageExtension").readTextThenPair()
            ?: entryPointNotFound(path)
    }
    initializeRealm()
    val moduleOrErr = parseModule(
        entryPointContent,
        runningExecutionContext.realm,
        entryPointPath.absolutePathString(),
    )
    val module = when (moduleOrErr) {
        is Valid -> moduleOrErr.value
        is Invalid -> {
            eprintln(moduleOrErr.value)
            exitProcess(1)
        }
    }
    module.loadRequestedModules()
        .onRejected {
            eprintln("failed to load modules requested by '${module.absolutePathWithoutExtension}': ${it.display()}")
        }
    module.link()
        .orReturn(::exitWithThrow)
    module.initializeEnvironment()
        .orReturn(::exitWithThrow)
    module.evaluate()
        .onRejected {
            eprintln("failed to evaluate module '${module.absolutePathWithoutExtension}': ${it.display()}")
        }
    runJobs()
}

private fun PromiseType.onRejected(callback: (reason: LanguageType) -> Unit) =
    also {
        then(null, BuiltinFunctionType(requiredParameterCount=1u) { _, args ->
            val reason = args[0]
            callback(reason)
            normalNull
        }, PromiseType.Capability.new())
    }

private inline fun entryPointNotFound(path: String): Nothing =
    throw NoSuchFileException(path)

private fun exitWithThrow(abrupt: Completion.Abrupt): Nothing {
    eprintln(abrupt.value!!.display())
    exitProcess(1)
}
