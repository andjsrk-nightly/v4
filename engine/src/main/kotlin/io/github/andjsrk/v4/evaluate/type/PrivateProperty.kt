package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.PrivateName

/**
 * The class only stores [data] since it can cover all the fields.
 */
data class PrivateProperty(val key: PrivateName, val data: Property)
