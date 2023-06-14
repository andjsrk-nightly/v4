package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

internal fun sameNullableValue(left: LanguageType?, right: LanguageType?): Boolean {
    return left == null && right == null || sameValue(left ?: return false, right ?: return false).value
}
