package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.builtin.RegExp

class RegExpType: ObjectType by ObjectType.Impl(lazy { RegExp.instancePrototype }) {
    val global = false
}
// TODO
