package io.github.andjsrk.v4.evaluate.type.lang

import java.util.*

@JvmInline
value class SymbolType(override val value: UUID = UUID.randomUUID()): PrimitiveLanguageType, PropertyKey
