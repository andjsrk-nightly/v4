package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.ThrowTrace
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NullType

typealias MaybeAbrupt<NormalV> = Completion<NormalV>
typealias NonEmpty = MaybeAbrupt<AbstractType>
typealias Empty = Completion.Normal<Nothing?>
typealias EmptyOrAbrupt = MaybeAbrupt<Nothing?>
typealias NormalOrAbrupt = MaybeAbrupt<LanguageType?>
typealias NonEmptyNormal = Completion.Normal<LanguageType>
typealias NonEmptyNormalOrAbrupt = MaybeAbrupt<LanguageType>

@EsSpec("Completion Record")
sealed interface Completion<out V: AbstractType?>: Record {
    val value: AbstractType?

    @EsSpec("normal completion")
    open class WideNormal<V: AbstractType?>(override val value: V): Completion<V>
    /**
     * A normal completion that only contains either a language value or `empty`.
     *
     * @see [WideNormal]
     */
    data class Normal<V: LanguageType?>(override val value: V): WideNormal<V>(value) {
        companion object {
            /**
             * Note that this covers `unused` as well.
             */
            val empty = Normal(null)
            val `null` = Normal(NullType)
        }
    }
    sealed interface Abrupt: Completion<Nothing> {
        override val value: LanguageType?
    }
    sealed interface NonEmptyAbrupt: Abrupt {
        override val value: LanguageType
    }
    data class Return(override val value: LanguageType): NonEmptyAbrupt
    data class Throw(override val value: LanguageType): NonEmptyAbrupt {
        init {
            ThrowTrace.instance = ThrowTrace(value)
        }
    }
    sealed interface IterationStop: Abrupt {
        val target: String?
    }
    data class Continue(override val value: LanguageType?, override val target: String?): IterationStop
    data class Break(override val value: LanguageType?, override val target: String?): IterationStop
}

internal inline val empty get() = Completion.Normal.empty
