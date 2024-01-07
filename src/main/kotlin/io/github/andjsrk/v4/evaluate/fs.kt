package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.error.BasicErrorKind
import io.github.andjsrk.v4.evaluate.type.toGeneralWideNormal
import io.github.andjsrk.v4.languageExtension
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.*

fun Path.resolveAndRead() =
    if (isDirectory()) TODO()
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
