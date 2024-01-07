package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.ThrowTrace
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NullType

typealias MaybeAbrupt<NormalV> = Completion<NormalV>
typealias Empty = Completion.Normal<Nothing?>
typealias EmptyOrAbrupt = MaybeAbrupt<Nothing?>
typealias NonEmptyWideOrAbrupt = MaybeAbrupt<AbstractType>
typealias MaybeEmpty = Completion.Normal<LanguageType?>
typealias MaybeEmptyOrAbrupt = MaybeAbrupt<LanguageType?>
typealias NonEmpty = Completion.Normal<LanguageType>
typealias NonEmptyOrAbrupt = MaybeAbrupt<LanguageType>

@EsSpec("Completion Record")
sealed interface Completion<out V: AbstractType?>: Record {
    val value: AbstractType?

    @EsSpec("normal completion")
    open class WideNormal<out V: AbstractType?>(override val value: V): Completion<V>
    /**
     * A normal completion that only contains either a language value or `empty`.
     *
     * @see [WideNormal]
     */
    data class Normal<out V: LanguageType?>(override val value: V): WideNormal<V>(value) {
        companion object {
            /**
             * Note that this covers `unused` as well.
             */
            val empty = Normal(null)
            /**
             * Indicates a normal completion that contains [NullType].
             */
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

inline val empty get() = Completion.Normal.empty
inline val normalNull get() = Completion.Normal.`null`

/**
 * Returns [normalNull] if the value is `null`, a normal completion that contains the value otherwise.
 */
inline fun LanguageType?.normalizeToNormal(): NonEmpty =
    this?.toNormal() ?: normalNull
inline fun <T: LanguageType?> T.toNormal() =
    Completion.Normal(this)
inline fun <T: AbstractType?> T.toWideNormal() =
    Completion.WideNormal(this)
inline fun <T> T.toGeneralWideNormal() =
    GeneralSpecValue(this).toWideNormal()
