package io.github.andjsrk.v4.evaluate.builtin.error

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.Object
import io.github.andjsrk.v4.evaluate.builtin.accessor
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*

/**
 * Note that `Error.prototype.name` has been changed to a getter that returns the object's class' name.
 */
@EsSpec("Error.prototype.name")
val errorNameGetter = getter("name") fn@ {
    val error = it.requireToBe<ObjectType> { return@fn it }
    error.findName()?.toLanguageValue().normalizeToNormal()
}

@EsSpec("%Error%")
val Error = BuiltinClassType(
    "Error",
    Object,
    mutableMapOf(),
    mutableMapOf(
        "name".accessor(getter=errorNameGetter, configurable=false),
    ),
    constructor(1u) ctor@ { error, args ->
        val message = args[0]
            .normalizeNull()
            ?.requireToBe<StringType> { return@ctor it }
        val options = args.getOptional(1)
            ?.requireToBe<ObjectType> { return@ctor it }
        if (message != null) error.createDataProperty("message".languageValue, message)
        error.initializeErrorCause(options)
            .orReturnThrow { return@ctor it }
        error.toNormal()
    },
)

private tailrec fun ObjectType.findName(): PropertyKey? {
    val proto = prototype ?: return null
    return proto.ownerClass?.name ?: proto.findName()
}

@EsSpec("InstallErrorCause")
private fun ObjectType.initializeErrorCause(options: ObjectType?): EmptyOrThrow {
    if (options == null) return empty
    val hasCause = options.hasProperty("cause".languageValue)
        .orReturnThrow { return it }
        .value
    if (hasCause) {
        val cause = options.get("cause".languageValue)
            .orReturnThrow { return it }
        createNonEnumerableDataPropertyOrThrow("cause".languageValue, cause)
            .unwrap()
    }
    return empty
}

internal fun createNativeErrorClass(name: String) =
    BuiltinClassType(
        name,
        Error,
        mutableMapOf(),
        mutableMapOf(),
        Error.constructor,
    )
