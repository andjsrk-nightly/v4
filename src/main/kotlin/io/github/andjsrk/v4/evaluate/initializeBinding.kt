package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.StringType
import io.github.andjsrk.v4.evaluate.type.spec.Environment
import io.github.andjsrk.v4.evaluate.type.spec.Reference
import io.github.andjsrk.v4.not

@EsSpec("InitializeReferencedBinding")
internal fun Reference.initializeBinding(value: LanguageType) {
    assert(this.not { isUnresolvable })
    require(base is Environment)
    require(referencedName is StringType)
    base.initializeBinding(referencedName.value, value)
}
