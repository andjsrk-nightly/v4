package io.github.andjsrk.v4

import io.github.andjsrk.v4.util.isOneOf

// 11.1.1 (https://tc39.es/ecma262/multipage/ecmascript-language-source-code.html#sec-utf16encodecodepoint)
const val specMaxCodePoint = 0x10FFFF

// 12.9.3 (https://tc39.es/ecma262/multipage/ecmascript-language-lexical-grammar.html#sec-literals-numeric-literals)
val Char.isSpecNumericLiteralSeparator get() =
    this == '_'
val Char.isSpecExponentIndicator get() =
    this == 'e' || this == 'E'
val Char.isSpecDecimalDigit get() =
    this in '0'..'9'
val Char.isSpecBinaryDigit get() =
    this == '0' || this == '1'
val Char.isSpecOctalDigit get() =
    this in '0'..'7'
val Char.isSpecHexDigit get() =
    this.isSpecDecimalDigit || this in 'a'..'f' || this in 'A'..'f'

// 12.2 (https://tc39.es/ecma262/multipage/ecmascript-language-lexical-grammar.html#sec-white-space)
val Char.isSpecWhiteSpace get() =
    this.isOneOf('\t', '\u000B', '\u000C', '\uFEFF') || this in CharCategory.SPACE_SEPARATOR

// 12.3 (https://tc39.es/ecma262/multipage/ecmascript-language-lexical-grammar.html#sec-line-terminators)
val Char.isSpecLineTerminator get() =
    this.isOneOf('\n', '\r', '\u2028', '\u2029')

// 12.7 (https://tc39.es/ecma262/multipage/ecmascript-language-lexical-grammar.html#prod-AsciiLetter)
val Char.isSpecAsciiLetter get() =
    this in 'a'..'z' || this in 'A'..'Z'
// changed; 12.7 (https://tc39.es/ecma262/multipage/ecmascript-language-lexical-grammar.html#prod-IdentifierName)
val Char.isSpecIdentifierName get() =
    this.isSpecAsciiLetter

// 12.9.4.2 (https://tc39.es/ecma262/multipage/ecmascript-language-lexical-grammar.html#table-string-single-character-escape-sequences)
private typealias UnescapedChar = Char
val specSingleEscapeCharacterMap =
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
