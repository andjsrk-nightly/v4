package io.github.andjsrk.v4.evaluate.builtin.number.static

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import java.text.DecimalFormat
import java.text.ParseException

@EsSpec("Number.parseFloat")
val parseLeadingDecimal = BuiltinFunctionType("parseLeadingDecimal", 1u) fn@ { _, args ->
    val string = args[0]
        .requireToBe<StringType> { return@fn it }
    // does not perform trim to input string
    val input = string.value
    run {
        val (sign, rest) = getSignAndRest(input)
        if (rest.startsWith("Infinity")) return@fn Completion.Normal(
            if (sign == 1) NumberType.POSITIVE_INFINITY
            else NumberType.NEGATIVE_INFINITY
        )
    }
    val uppercaseInput = input
        .uppercase() // DecimalFormat does not recognize scientific notation with lowercase 'e', so making it uppercase
    val parsed =
        try {
            DecimalFormat.getInstance().parse(uppercaseInput)
                .toDouble()
                .languageValue
        } catch (e: ParseException) {
            NumberType.NaN
        }
    Completion.Normal(parsed)
}
