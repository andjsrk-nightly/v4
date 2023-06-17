package io.github.andjsrk.v4.evaluate.type

class NormalImportEntry(
    sourceModule: String,
    val importName: String,
    localName: String,
): ImportEntry(sourceModule, localName)
