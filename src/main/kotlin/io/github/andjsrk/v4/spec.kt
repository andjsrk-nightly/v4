package io.github.andjsrk.v4

import io.github.andjsrk.v4.isOneOf

/**
 * See [UTF16EncodeCodePoint](https://tc39.es/ecma262/multipage/ecmascript-language-source-code.html#sec-utf16encodecodepoint).
 */
@EsSpec("-")
const val MAX_CODE_POINT = 0x10FFFF

@EsSpec("WhiteSpace")
val Char.isWhiteSpace get() =
    this.isOneOf('\t', '\u000B', '\u000C', '\uFEFF') || this in CharCategory.SPACE_SEPARATOR

@EsSpec("LineTerminator")
val Char.isLineTerminator get() =
    this.isOneOf('\n', '\r', '\u2028', '\u2029')

@EsSpec("AsciiLetter")
val Char.isAsciiLetter get() =
    this in 'a'..'z' || this in 'A'..'Z'

/**
 * modified to:
 *
 * IdentifierChar `::`
 *   AsciiLetter
 *   `_`
 *   `$`
 *
 * IdentifierName `::`
 *   IdentifierChar
 *   IdentifierName IdentifierChar
 */
@EsSpec("IdentifierName")
val Char.isIdentifierChar get() =
    this.isAsciiLetter || this == '_' || this == '$'

@EsSpec("NumericLiteralSeparator")
val Char.isNumericLiteralSeparator get() =
    this == '_'
@EsSpec("ExponentIndicator")
val Char.isExponentIndicator get() =
    this == 'e' || this == 'E'
@EsSpec("DecimalDigit")
val Char.isDecimalDigit get() =
    this in '0'..'9'
@EsSpec("BinaryDigit")
val Char.isBinaryDigit get() =
    this == '0' || this == '1'
@EsSpec("OctalDigit")
val Char.isOctalDigit get() =
    this in '0'..'7'
@EsSpec("HexDigit")
val Char.isHexDigit get() =
    this.isDecimalDigit || this in 'a'..'f' || this in 'A'..'f'

private typealias UnescapedChar = Char

/**
 * See [Table 37](https://tc39.es/ecma262/multipage/ecmascript-language-lexical-grammar.html#table-string-single-character-escape-sequences).
 */
@EsSpec("-")
val singleEscapeCharacterMap =
    mapOf<Char, UnescapedChar>(
        '\'' to '\'',
        '"' to '"',
        '\\' to '\\',
        'b' to '\b',
        'f' to '\u000C',
        'n' to '\n',
        'r' to '\r',
        't' to '\t',
        'v' to '\u000B',
    )
