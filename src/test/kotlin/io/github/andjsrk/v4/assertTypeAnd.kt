package io.github.andjsrk.v4

import kotlin.test.assertIs

internal inline fun <reified T> Any?.assertTypeAnd(block: T.() -> Unit) {
    assertIs<T>(this)
    run(block)
}
