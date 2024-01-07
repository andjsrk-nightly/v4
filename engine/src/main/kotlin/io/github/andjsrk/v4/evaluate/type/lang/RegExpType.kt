package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.builtin.RegExp

class RegExpType: ObjectType(lazy { RegExp.instancePrototype }) {
    val global = false
}
// TODO
