package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec

@EsSpec("ImportEntry Record")
sealed class ImportEntry(val sourceModule: String, val localName: String): Record
