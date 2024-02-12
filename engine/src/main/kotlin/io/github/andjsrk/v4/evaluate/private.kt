package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.not

typealias PrivateName = StringType

@EsSpec("PrivateGet")
fun LanguageType.privateGet(key: PrivateName): NonEmptyOrThrow {
    if (this !is ObjectType) return privateAccessDenied(key)
    val elem = privateElements[key] ?: return privateAccessDenied(key)
    if (elem.data is AccessorProperty && elem.data.get == null) return throwError(TypeErrorKind.INVALID_PRIVATE_GETTER_ACCESS, key.value)
    return elem.data.getValue(this, key)
}
@EsSpec("PrivateSet")
fun LanguageType.privateSet(key: PrivateName, value: LanguageType): EmptyOrThrow {
    if (this !is ObjectType) return privateAccessDenied(key)
    val elem = privateElements[key] ?: return privateAccessDenied(key)
    val isMethodProp = elem.data is DataProperty && elem.data.not { writable }
    if (isMethodProp) return throwError(TypeErrorKind.INVALID_PRIVATE_METHOD_WRITE, key.value)
    if (elem.data is AccessorProperty && elem.data.set == null) return throwError(TypeErrorKind.INVALID_PRIVATE_SETTER_ACCESS, key.value)
    return elem.data.setValue(this, key, value)
}

private fun LanguageType.privateAccessDenied(key: PrivateName) =
    throwError(TypeErrorKind.PRIVATE_NAME_ACCESS_FROM_OUTSIDE, key.value, display())
