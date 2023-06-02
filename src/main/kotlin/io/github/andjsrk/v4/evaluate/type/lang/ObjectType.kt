package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.type.spec.Property

@JvmInline
value class ObjectType(override val value: MutableMap<LanguageType, Property>): LanguageType
