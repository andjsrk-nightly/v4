package io.github.andjsrk.v4.evaluate.type.spec

import io.github.andjsrk.v4.EsSpec

@EsSpec("ImportEntry Record")
sealed class ImportEntry(val sourceModule: String, val localName: String): Record
