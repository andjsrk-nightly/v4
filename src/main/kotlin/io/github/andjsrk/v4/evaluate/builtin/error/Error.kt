package io.github.andjsrk.v4.evaluate.builtin.error

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
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
        "name".languageValue to AccessorProperty(nameGetter, configurable=false),
    ),
    constructor(1u) ctor@ { error, args ->
        val message = args[0]
            .normalizeNull()
            .requireToBeNullable<StringType> { return@ctor it }
        val options = args.getOrNull(1)
            .normalizeNull()
            .requireToBeNullable<ObjectType> { return@ctor it }
        if (message != null) error.createNonEnumerablePropertyOrThrow("message".languageValue, message)
        returnIfAbrupt(error.initializeErrorCause(options)) { return@ctor it }
        Completion.Normal(error)
    },
)

@EsSpec("InstallErrorCause")
private fun ObjectType.initializeErrorCause(options: ObjectType?): EmptyOrAbrupt {
    if (options == null) return empty
    if (options.hasProperty("cause".languageValue)) {
        val cause = returnIfAbrupt(options.get("cause".languageValue)) { return it }
        createNonEnumerablePropertyOrThrow("cause".languageValue, cause)
    }
    return empty
}
