package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NullType

typealias NonEmpty = Completion<AbstractType>
typealias MaybeAbrupt<NormalV> = Completion<NormalV>
typealias Empty = Completion.Normal<Nothing?>
typealias EmptyOrAbrupt = Completion<Nothing?>
typealias NormalOrAbrupt = Completion<LanguageType?>
typealias NonEmptyNormal = Completion.Normal<LanguageType>
typealias NonEmptyNormalOrAbrupt = Completion<LanguageType>

@EsSpec("Completion Record")
sealed interface Completion<out V: AbstractType?>: Record {
    val value: AbstractType?

    open class WideNormal<V: AbstractType?>(override val value: V): Completion<V>
    class Normal<V: LanguageType?>(override val value: V): WideNormal<V>(value) {
        companion object {
            /**
             * Note that this covers `unused` as well.
             */
            val empty = Normal(null)
            val `null` = Normal(NullType)
        }
    }
    sealed class Abrupt(override val value: LanguageType?): Completion<Nothing>
    sealed class NonEmptyAbrupt(override val value: LanguageType): Abrupt(value)
    class Return(value: LanguageType): NonEmptyAbrupt(value)
    class Throw(value: LanguageType): NonEmptyAbrupt(value)
    sealed class IterationStop(value: LanguageType?, val target: String?): Abrupt(value)
    class Continue(value: LanguageType?, target: String?): IterationStop(value, target)
    class Break(value: LanguageType?, target: String?): IterationStop(value, target)
}

internal inline val empty get() = Completion.Normal.empty
