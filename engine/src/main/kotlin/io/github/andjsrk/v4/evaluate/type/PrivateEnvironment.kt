package io.github.andjsrk.v4.evaluate.type

class PrivateEnvironment(override val outer: PrivateEnvironment?): Environment(outer) {
    val names = mutableSetOf<PrivateName>()
    override fun hasBinding(name: String): MaybeThrow<BooleanType> {
        TODO()
    }
    override fun createMutableBinding(name: String): EmptyOrThrow {
        TODO()
    }
    override fun createImmutableBinding(name: String): EmptyOrThrow {
        TODO()
    }
    override fun initializeBinding(name: String, value: LanguageType): EmptyOrThrow {
        TODO()
    }
    override fun setMutableBinding(name: String, value: LanguageType): EmptyOrThrow {
        TODO()
    }
    override fun getBindingValue(name: String): NonEmptyOrThrow {
        TODO()
    }
}
