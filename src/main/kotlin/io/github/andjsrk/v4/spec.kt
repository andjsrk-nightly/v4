package io.github.andjsrk.v4

import io.github.andjsrk.v4.util.isOneOf

// 11.1.1 (https://tc39.es/ecma262/multipage/ecmascript-language-source-code.html#sec-utf16encodecodepoint)
@EsSpec
const val maxCodePoint = 0x10FFFF

// 12.2 (https://tc39.es/ecma262/multipage/ecmascript-language-lexical-grammar.html#sec-white-space)
@EsSpec
val Char.isWhiteSpace get() =
    this.isOneOf('\t', '\u000B', '\u000C', '\uFEFF') || this in CharCategory.SPACE_SEPARATOR

// 12.3 (https://tc39.es/ecma262/multipage/ecmascript-language-lexical-grammar.html#sec-line-terminators)
@EsSpec
val Char.isLineTerminator get() =
    this.isOneOf('\n', '\r', '\u2028', '\u2029')

// 12.7 (https://tc39.es/ecma262/multipage/ecmascript-language-lexical-grammar.html#prod-AsciiLetter)
@EsSpec
val Char.isAsciiLetter get() =
    this in 'a'..'z' || this in 'A'..'Z'
// changed; 12.7 (https://tc39.es/ecma262/multipage/ecmascript-language-lexical-grammar.html#prod-IdentifierName)
@EsSpec
val Char.isIdentifierName get() =
    this.isAsciiLetter || this == '_' || this == '$'

// 12.9.3 (https://tc39.es/ecma262/multipage/ecmascript-language-lexical-grammar.html#sec-literals-numeric-literals)
@EsSpec
val Char.isNumericLiteralSeparator get() =
    this == '_'
@EsSpec
val Char.isExponentIndicator get() =
    this == 'e' || this == 'E'
@EsSpec
val Char.isDecimalDigit get() =
    this in '0'..'9'
@EsSpec
val Char.isBinaryDigit get() =
    this == '0' || this == '1'
@EsSpec
val Char.isOctalDigit get() =
    this in '0'..'7'
@EsSpec
val Char.isHexDigit get() =
    this.isDecimalDigit || this in 'a'..'f' || this in 'A'..'f'

// 12.9.4.2 (https://tc39.es/ecma262/multipage/ecmascript-language-lexical-grammar.html#table-string-single-character-escape-sequences)
private typealias UnescapedChar = Char
@EsSpec
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
