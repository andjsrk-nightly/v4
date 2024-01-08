package io.github.andjsrk.v4.cli

import io.github.andjsrk.v4.error.BasicErrorKind
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.*
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.*

const val languageExtension = ".ss"

fun Path.resolveAndRead(): MaybeAbrupt<GeneralSpecValue<Pair<Path, String>>> =
    if (isDirectory()) TODO("Treating a directory as an entry point is not implemented yet")
    else readTextThenPair()?.toGeneralWideNormal()
        ?: Path("${absolutePathString()}$languageExtension").readTextThenPair()?.toGeneralWideNormal()
        ?: throwError(BasicErrorKind.CANNOT_FIND_MODULE, absolutePathString())

fun Path.join(other: String) =
    resolveSibling(other).normalize()

fun Path.readTextThenPair() =
    readTextOrNull()?.let { this to it }

private inline fun Path.readTextOrNull() =
    try {
        readText()
    } catch (e: NoSuchFileException) {
        null
    }
