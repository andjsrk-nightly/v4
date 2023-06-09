package io.github.andjsrk.v4.evaluate.type.spec

class NormalImportEntry(
    sourceModule: String,
    val importName: String,
    localName: String,
): ImportEntry(sourceModule, localName)
