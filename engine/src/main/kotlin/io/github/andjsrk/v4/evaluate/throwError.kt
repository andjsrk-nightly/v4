package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.error.ErrorKind
import io.github.andjsrk.v4.evaluate.type.Completion

inline fun throwError(kind: ErrorKind, vararg args: String) =
    Completion.Throw(error(kind, *args))
