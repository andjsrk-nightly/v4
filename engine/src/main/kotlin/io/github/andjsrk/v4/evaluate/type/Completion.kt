package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.SimpleLazyFlow
import io.github.andjsrk.v4.evaluate.ThrowTrace

typealias MaybeEmpty = Completion.Normal<LanguageType?>
typealias Empty = Completion.Normal<Nothing?>
typealias NonEmpty = Completion.Normal<LanguageType>

typealias MaybeAbrupt<V> = Completion<V>
typealias NonEmptyWideOrAbrupt = MaybeAbrupt<EvaluationResult>
typealias MaybeEmptyOrAbrupt = MaybeAbrupt<LanguageType?>
typealias EmptyOrAbrupt = MaybeAbrupt<Nothing?>
typealias NonEmptyOrAbrupt = MaybeAbrupt<LanguageType>

typealias MaybeThrow<V> = Completion.WideNormalOrThrow<V>
typealias EmptyOrThrow = MaybeThrow<Nothing?>
typealias NonEmptyOrThrow = MaybeThrow<LanguageType>

typealias EmptyOrNonEmptyAbrupt = Completion.FromFunctionBody<Nothing?>

@EsSpec("Completion Record")
sealed interface Completion<out V: AbstractType?>: Record {
    val value: AbstractType?

    /**
     * Indicates an union of [WideNormal], [NonEmptyAbrupt].
     * Note that the union should contain [Normal] instead of [WideNormal] originally,
     * but if the union contains [Normal] it drops convenience extremely on when expressions with [WideNormalOrThrow].
     */
    sealed interface FromFunctionBody<out V: AbstractType?>: Completion<V>

    /**
     * Indicates an union of [WideNormal], [Throw].
     */
    sealed interface WideNormalOrThrow<out V: AbstractType?>: FromFunctionBody<V>
    @EsSpec("normal completion")
    open class WideNormal<out V: AbstractType?>(override val value: V): WideNormalOrThrow<V>
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
    sealed interface NonEmptyAbrupt: Abrupt, FromFunctionBody<Nothing> {
        override val value: LanguageType
    }
    data class Return(override val value: LanguageType): NonEmptyAbrupt
    data class Throw(override val value: LanguageType): NonEmptyAbrupt, WideNormalOrThrow<Nothing> {
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

/**
 * Unsafely casts the [SimpleLazyFlow] to contain a [Completion.FromFunctionBody].
 */
fun <T: AbstractType?> SimpleLazyFlow<Completion<T>>.asFromFunctionBody() =
    this as SimpleLazyFlow<Completion.FromFunctionBody<T>>
/**
 * Unsafely casts the [SimpleLazyFlow] to contain a [MaybeThrow].
 */
fun <T: AbstractType?> SimpleLazyFlow<Completion<T>>.asMaybeThrow() =
    this as SimpleLazyFlow<MaybeThrow<T>>

fun <T: AbstractType> SimpleLazyFlow<Completion.FromFunctionBody<T?>>.asNotNullable() =
    this as SimpleLazyFlow<Completion.FromFunctionBody<T>>
