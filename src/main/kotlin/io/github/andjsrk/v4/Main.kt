package io.github.andjsrk.v4

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.parse.Parser
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.readText
import kotlin.system.exitProcess

const val extension = ".ss"

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
    var entryPointContent = cwd.resolve(path).let {
        if (it.isDirectory()) TODO("Treating a directory as an entry point is not implemented yet")
        else it.readTextOrNull()
            ?: cwd.resolve("$path$extension").readTextOrNull()
            ?: entryPointNotFound(path)
    }
    initializeRealm()
    val module = when (val moduleOrErr = parseModule(entryPointContent, runningExecutionContext.realm)) {
        is Valid -> moduleOrErr.value
        is Invalid -> {
            eprintln(moduleOrErr.value)
            exitProcess(1)
        }
    }
    module.initializeEnvironment()
        .orReturn(::exitWithThrow)
    module.executeModule()
        .orReturn(::exitWithThrow)
}

private inline fun Path.readTextOrNull() =
    try {
        readText()
    } catch (e: NoSuchFileException) {
        null
    }

private inline fun entryPointNotFound(path: String): Nothing =
    throw NoSuchFileException(path)

private fun exitWithThrow(abrupt: Completion.Abrupt): Nothing {
    eprintln(abrupt.value!!.display())
    exitProcess(1)
}

private fun eprintln(value: Any?) =
    System.err.println(value)
