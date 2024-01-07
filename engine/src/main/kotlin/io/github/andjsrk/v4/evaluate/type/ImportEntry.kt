package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec

@EsSpec("ImportEntry Record")
sealed class ImportEntry(open val sourceModule: String, open val localName: String): Record
