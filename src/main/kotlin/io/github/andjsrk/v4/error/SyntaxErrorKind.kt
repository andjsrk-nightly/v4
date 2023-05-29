package io.github.andjsrk.v4.error

enum class SyntaxErrorKind(override val message: String): ErrorKind {
    AMBIGUOUS_EXPORT("The requested module '%' contains conflicting star exports for name '%'"),
    ARG_STRING_TERMINATES_PARAMETERS_EARLY("Arg string terminates parameters early"),
    ASYNC_FUNCTION_IN_SINGLE_STATEMENT_CONTEXT("Async functions can only be declared at the top level or inside a block."),
    AWAIT_EXPRESSION_FORMAL_PARAMETER("Await expression not allowed in formal parameter"),
    BAD_GETTER_ARITY("Getter must not have any formal parameters."),
    BAD_SETTER_ARITY("Setter must have exactly one formal parameter."),
    BAD_SETTER_REST_PARAMETER("Setter function argument must not be a rest parameter"),
    BIGINT_INVALID_STRING("Invalid BigInt string"),
    CONSTRUCTOR_IS_ACCESSOR("Class constructor may not be an accessor"),
    CONSTRUCTOR_IS_ASYNC("Class constructor may not be an async method"),
    CONSTRUCTOR_IS_GENERATOR("Class constructor may not be a generator"),
    CONSTRUCTOR_IS_PRIVATE("Class constructor may not be a private method"),
    DECLARATION_MISSING_INITIALIZER("Missing initializer in declaration"),
    DERIVED_CONSTRUCTOR_RETURNED_NON_OBJECT("Derived constructors may only return object or undefined"),
    DUPLICATE_CONSTRUCTOR("A class may only have one constructor"),
    DUPLICATE_CLASS_ELEMENT_NAMES("Duplicate class element name not allowed"),
    DUPLICATE_EXPORT("Duplicate export of '%'"),
    DUPLICATE_PARAMETER_NAMES("Duplicate parameter name not allowed"),
    DUPLICATE_PROTO("Duplicate __proto__ fields are not allowed in object literals"),
    ELEMENT_AFTER_REST("Rest element must be last element"),
    FOUND_NON_CALLABLE_HAS_INSTANCE("Found non-callable @@hasInstance"),
    ILLEGAL_BREAK("Illegal break statement"),
    ILLEGAL_CONTINUE("Illegal continue statement: '%' does not denote an iteration statement"),
    ILLEGAL_RETURN("Illegal return statement"),
    INTRINSIC_WITH_SPREAD("Intrinsic calls do not support spread arguments"),
    INVALID_REST_BINDING_PATTERN("`...` must be followed by an identifier in declaration contexts"),
    INVALID_PROPERTY_BINDING_PATTERN("Illegal property in declaration context"),
    INVALID_COVER_INITIALIZED_NAME("Invalid shorthand property initializer"),
    INVALID_DESTRUCTURING_TARGET("Invalid destructuring assignment target"),
    INVALID_HEX_ESCAPE_SEQUENCE("Invalid hexadecimal escape sequence"),
    INVALID_UNICODE_ESCAPE_SEQUENCE("Invalid Unicode escape sequence"),
    INVALID_LHS_IN_ASSIGNMENT("Invalid left-hand side in assignment"),
    INVALID_LHS_IN_FOR("Invalid left-hand side in for-loop"),
    INVALID_LHS_IN_POSTFIX_OP("Invalid left-hand side expression in postfix operation"),
    INVALID_LHS_IN_PREFIX_OP("Invalid left-hand side expression in prefix operation"),
    INVALID_MODULE_EXPORT_NAME("Invalid module export name: contains unpaired surrogate"),
    INVALID_OR_UNEXPECTED_TOKEN("Invalid or unexpected token"),
    INVALID_PRIVATE_BRAND_INSTANCE("Receiver must be an instance of class %"),
    INVALID_PRIVATE_BRAND_STATIC("Receiver must be class %"),
    INVALID_PRIVATE_BRAND_REINITIALIZATION("Cannot initialize private methods of class % twice on the same object"),
    INVALID_PRIVATE_FIELD_REINITIALIZATION("Cannot initialize % twice on the same object"),
    INVALID_PRIVATE_FIELD_RESOLUTION("Private field '%' must be declared in an enclosing class"),
    INVALID_PRIVATE_MEMBER_READ("Cannot read private member % from an object whose class did not declare it"),
    INVALID_PRIVATE_MEMBER_WRITE("Cannot write private member % to an object whose class did not declare it"),
    INVALID_PRIVATE_METHOD_WRITE("Private method '%' is not writable"),
    INVALID_PRIVATE_GETTER_ACCESS("'%' was defined without a getter"),
    INVALID_PRIVATE_SETTER_ACCESS("'%' was defined without a setter"),
    INVALID_REGEXP_FLAGS("Invalid flags supplied to RegExp constructor '%'"),
    INVALID_REST_ASSIGNMENT_PATTERN("`...` must be followed by an assignable reference in assignment contexts"),
    INVALID_UNUSED_PRIVATE_STATIC_METHOD_ACCESSED_BY_DEBUGGER("Unused static private method '%' cannot be accessed at debug time"),
    JSON_PARSE_BAD_CONTROL_CHARACTER("Bad control character in string literal in JSON at position %"),
    JSON_PARSE_BAD_ESCAPED_CHARACTER("Bad escaped character in JSON at position %"),
    JSON_PARSE_BAD_UNICODE_ESCAPE("Bad Unicode escape in JSON at position %"),
    JSON_PARSE_EXPECTED_COLON_AFTER_PROPERTY_NAME("Expected ':' after property name in JSON at position %"),
    JSON_PARSE_EXPECTED_COMMA_OR_RIGHT_BRACE("Expected ',' or '}' after property value in JSON at position %"),
    JSON_PARSE_EXPECTED_COMMA_OR_RIGHT_BRACKET("Expected ',' or ']' after array element in JSON at position %"),
    JSON_PARSE_EXPECTED_DOUBLE_QUOTED_PROPERTY_NAME("Expected double-quoted property name in JSON at position %"),
    JSON_PARSE_EXPECTED_PROP_NAME_OR_RIGHT_BRACE("Expected property name or '}' in JSON at position %"),
    JSON_PARSE_EXPONENT_PART_MISSING_NUMBER("Exponent part is missing a number in JSON at position %"),
    JSON_PARSE_NO_NUMBER_AFTER_MINUS_SIGN("No number after minus sign in JSON at position %"),
    JSON_PARSE_SHORT_STRING("\"%\" is not valid JSON"),
    JSON_PARSE_UNEXPECTED_EOS("Unexpected end of JSON input"),
    JSON_PARSE_UNEXPECTED_NON_WHITE_SPACE_CHARACTER("Unexpected non-whitespace character after JSON at position  %"),
    JSON_PARSE_UNEXPECTED_TOKEN_END_STRING_WITH_CONTEXT("Unexpected token '%', ...\"%\" is not valid JSON"),
    JSON_PARSE_UNEXPECTED_TOKEN_NUMBER("Unexpected number in JSON at position %"),
    JSON_PARSE_UNEXPECTED_TOKEN_STRING("Unexpected string in JSON at position %"),
    JSON_PARSE_UNEXPECTED_TOKEN_SHORT_STRING("Unexpected token '%', \"%\" is not valid JSON"),
    JSON_PARSE_UNEXPECTED_TOKEN_START_STRING_WITH_CONTEXT("Unexpected token '%', \"%\"... is not valid JSON"),
    JSON_PARSE_UNEXPECTED_TOKEN_SURROUND_STRING_WITH_CONTEXT("Unexpected token '%', ...\"%\"... is not valid JSON"),
    JSON_PARSE_UNTERMINATED_FRACTIONAL_NUMBER("Unterminated fractional number in JSON at position %"),
    JSON_PARSE_UNTERMINATED_STRING("Unterminated string in JSON at position %"),
    LABEL_REDECLARATION("Label '%' has already been declared"),
    MALFORMED_ARROW_FUN_PARAM_LIST("Malformed arrow function parameter list"),
    MALFORMED_REGEXP("Invalid regular expression: /%/%: %"),
    MALFORMED_REGEXP_FLAGS("Invalid regular expression flags"),
    MISSING_CLASS_NAME("Class declarations require a name"),
    MODULE_EXPORT_NAME_WITHOUT_FROM_CLAUSE("String literal module export names must be followed by a 'from' clause"),
    MODULE_EXPORT_UNDEFINED("Export '%' is not defined in module"),
    MULTIPLE_DEFAULTS_IN_SWITCH("More than one default clause in switch statement"),
    NEW_OPTIONAL_CHAIN("Invalid optional chain from new expression"),
    NEWLINE_AFTER_THROW("Illegal newline after throw"),
    NEWLINE_AFTER_YIELD("Illegal newline after yield"),
    NO_CATCH_OR_FINALLY("Missing catch or finally after try"),
    NO_ITERATION_STATEMENT("Illegal continue statement: no surrounding iteration statement"),
    REST_DEFAULT_INITIALIZER("Rest parameter may not have a default initializer"),
    RUNTIME_WRONG_NUM_ARGS("Runtime function given wrong number of arguments"),
    SINGLE_FUNCTION_LITERAL("Single function literal required"),
    SPECIES_NOT_CONSTRUCTOR("object.constructor[Symbol.species] is not a constructor"),
    STRICT_EVAL_ARGUMENTS("Unexpected eval or arguments in strict mode"),
    SUPER_OPTIONAL_CHAIN("Invalid optional chain from super"),
    TAGGED_TEMPLATE_OPTIONAL_CHAIN("Invalid tagged template on optional chain"),
    THIS_FORMAL_PARAMETER("'this' is not a valid formal parameter name"),
    TOO_MANY_ARGUMENTS("Too many arguments in function call (only 65535 allowed)"),
    TOO_MANY_ELEMENTS_IN_PROMISE_COMBINATOR("Too many elements passed to Promise.%"),
    TOO_MANY_PARAMETERS("Too many parameters in function definition (only 65534 allowed)"),
    TOO_MANY_PROPERTIES("Too many properties to enumerate"),
    TOO_MANY_SPREADS("Literal containing too many nested spreads (up to 65534 allowed)"),
    TOO_MANY_VARIABLES("Too many variables declared (only 4194303 allowed)"),
    TYPED_ARRAY_TOO_SHORT("Derived TypedArray constructor created an array which was too small"),
    UNDEFINED_UNICODE_CODE_POINT("Undefined Unicode code-point"),
    UNEXPECTED_END_OF_ARG_STRING("Unexpected end of arg string"),
    UNEXPECTED_EOS("Unexpected end of input"),
    UNEXPECTED_PRIVATE_FIELD("Unexpected private field"),
    UNEXPECTED_RESERVED("Unexpected reserved word"),
    UNEXPECTED_SUPER("'super' keyword unexpected here"),
    UNEXPECTED_TEMPLATE_STRING("Unexpected template string"),
    UNEXPECTED_TOKEN("Unexpected token '%'"),
    UNEXPECTED_TOKEN_IDENTIFIER("Unexpected identifier '%'"),
    UNEXPECTED_TOKEN_NUMBER("Unexpected number"),
    UNEXPECTED_TOKEN_STRING("Unexpected string"),
    UNEXPECTED_TOKEN_UNARY_EXPONENTIATION("Unary operator used immediately before exponentiation expression. Parenthesis must be used to disambiguate operator precedence"),
    UNEXPECTED_TOKEN_REG_EXP("Unexpected regular expression"),
    UNEXPECTED_LEXICAL_DECLARATION("Lexical declaration cannot appear in a single-statement context"),
    UNKNOWN_LABEL("Undefined label '%'"),
    UNRESOLVABLE_EXPORT("The requested module '%' does not provide an export named '%'"),
    UNTERMINATED_ARG_LIST("missing ) after argument list"),
    UNTERMINATED_REG_EXP("Invalid regular expression: missing /"),
    UNTERMINATED_TEMPLATE("Unterminated template literal"),
    UNTERMINATED_TEMPLATE_EXPR("Missing } in template expression"),
    VAR_REDECLARATION("Identifier '%' has already been declared"),
    YIELD_IN_PARAMETER("Yield expression not allowed in formal parameter"),
}
