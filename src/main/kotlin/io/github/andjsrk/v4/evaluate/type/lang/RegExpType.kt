package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.builtin.regexp.RegExp

class RegExpType: ObjectType(lazy { RegExp.instancePrototype })
// TODO
