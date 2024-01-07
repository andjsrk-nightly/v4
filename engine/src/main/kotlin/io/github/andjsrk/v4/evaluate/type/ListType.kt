package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec

@EsSpec("List")
@JvmInline
value class ListType<out E>(val list: List<E>): AbstractType, List<E> by list
