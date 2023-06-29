package io.github.andjsrk.v4.evaluate.builtin.number.static

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.requireToBe
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
        .uppercase() // DecimalFormat does not recognize scientific notation with lowercase 'e', so making it uppercase
    val parsed = try {
        DecimalFormat.getInstance().parse(input).toDouble().languageValue
    } catch (e: ParseException) {
        NumberType.NaN
    }
    Completion.Normal(parsed)
}
