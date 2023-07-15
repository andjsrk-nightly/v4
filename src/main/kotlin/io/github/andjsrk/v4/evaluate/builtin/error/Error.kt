package io.github.andjsrk.v4.evaluate.builtin.error

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.accessor
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor

@EsSpec("%Error%")
val Error = BuiltinClassType(
    "Error",
    Object,
    mutableMapOf(
        // TODO
    ),
    mutableMapOf(
        "name".accessor(getter=nameGetter, configurable=false),
    ),
    constructor(1u) ctor@ { error, args ->
        val message = args[0]
            .normalizeNull()
            ?.requireToBe<StringType> { return@ctor it }
        val options = args.getOptional(1)
            ?.requireToBe<ObjectType> { return@ctor it }
        if (message != null) error.createDataProperty("message".languageValue, message)
        error.initializeErrorCause(options)
            .returnIfAbrupt { return@ctor it }
        Completion.Normal(error)
    },
)

@EsSpec("InstallErrorCause")
private fun ObjectType.initializeErrorCause(options: ObjectType?): EmptyOrAbrupt {
    if (options == null) return empty
    if (options.hasProperty("cause".languageValue)) {
        val cause = options.get("cause".languageValue)
            .returnIfAbrupt { return it }
        createNonEnumerablePropertyOrThrow("cause".languageValue, cause)
    }
    return empty
}
