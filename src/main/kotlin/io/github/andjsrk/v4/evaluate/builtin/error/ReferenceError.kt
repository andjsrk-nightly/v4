package io.github.andjsrk.v4.evaluate.builtin.error

import io.github.andjsrk.v4.EsSpec

@EsSpec("%ReferenceError%")
val ReferenceError = createNativeErrorClass("ReferenceError")
