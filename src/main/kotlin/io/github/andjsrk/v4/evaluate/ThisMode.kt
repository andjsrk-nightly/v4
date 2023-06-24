package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec

/**
 * See [`[[ThisMode]]`](https://tc39.es/ecma262/multipage/ordinary-and-exotic-objects-behaviours.html#table-internal-slots-of-ecmascript-function-objects).
 */
@EsSpec("-")
enum class ThisMode {
    ARROW, // lexical
    METHOD // strict
}
