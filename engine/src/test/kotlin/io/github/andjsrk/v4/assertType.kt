package io.github.andjsrk.v4

import kotlin.test.assertIs

internal inline fun <reified T> Any?.assertType(): T {
    assertIs<T>(this)
    return this
}

internal inline fun <reified T> Any?.assertTypeAnd(block: T.() -> Unit) =
    this.assertType<T>()
        .run(block)
