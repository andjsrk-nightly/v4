package io.github.andjsrk.v4.evaluate.type

/**
 * The class only stores [data] since it can cover all the fields.
 */
data class PrivateProperty(val key: PrivateName, val data: Property): ClassElementEvaluationResult {
    init {
        if (data is DataProperty) {
            require(data.value is FunctionType) { "The property is not a method property." }
        }
    }
}
