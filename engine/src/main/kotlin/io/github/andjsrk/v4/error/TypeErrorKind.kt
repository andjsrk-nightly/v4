package io.github.andjsrk.v4.error

enum class TypeErrorKind(override val message: String): ErrorKind {
    AMBIGUOUS_PROPERTY_DESCRIPTOR("Ambiguous property descriptor: cannot determine which needs to be used either accessors or value/writable properties"),
    ANONYMOUS_CONSTRUCTOR_NON_CALLABLE("Class constructors cannot be invoked without 'new'"),
    APPLY_NON_FUNCTION("Function.prototype.apply was called on %, which is % and not a function"),
    ARRAY_BUFFER_DETACH_KEY_DOESNT_MATCH("Provided key doesn't match [[ArrayBufferDetachKey]]"),
    ARRAY_BUFFER_SPECIES_THIS("ArrayBuffer subclass returned this from species constructor"),
    ARRAY_BUFFER_TOO_SHORT("Derived ArrayBuffer constructor created a buffer which was too small"),
    ATOMICS_MUTEX_NOT_OWNED_BY_CURRENT_THREAD("Atomics.Mutex is not owned by the current agent"),
    ATOMICS_OPERATION_NOT_ALLOWED("% cannot be called in this context"),
    AWAIT_NOT_IN_ASYNC_CONTEXT("await is only valid in async functions and the top level bodies"),
    BAD_ROUNDING_TYPE("RoundingType is not fractionDigits"),
    BAD_SORT_COMPARISON_FUNCTION("The comparison function must be either a function or undefined"),
    BIGINT_FROM_INVALID_VALUE("Cannot convert % to a BigInt"),
    BIGINT_FROM_NON_INTEGER("The number % cannot be converted to a BigInt because it is not an integer"),
    BIGINT_MIXED_TYPES("Cannot mix BigInt and other types, use explicit conversions"),
    BIGINT_SHR("BigInts have no unsigned right shift, use >> instead"),
    BIGINT_TO_NUMBER("Cannot convert a BigInt value to a number"),
    BOOLEAN_FROM_INVALID_VALUE("Cannot convert % to a boolean"),
    CALLED_NON_CALLABLE("% is not a function"),
    CALLED_ON_NON_OBJECT("% called on non-object"),
    CALLED_ON_NULL_OR_UNDEFINED("% called on null or undefined"),
    CANNOT_ASSIGN_TO_READ_ONLY_PROPERTY("Cannot assign to read only property '%'"),
    CANNOT_COMPARE_NAN("Cannot compare NaN with a number"),
    CANNOT_CONSTRUCT("Cannot construct an instance of %"),
    CANNOT_CONVERT_TO_STRING("Cannot convert object to a string"),
    CANNOT_CONVERT_BIGINT_TO_JSON("Cannot convert a BigInt to JSON representation since it have ambiguity with a number"),
    CANNOT_CONVERT_FUNCTION_TO_JSON("Cannot convert a function to JSON representation"),
    CANNOT_CONVERT_SYMBOL_TO_JSON("Cannot convert a symbol to JSON representation"),
    CANNOT_DELETE_PROPERTY("Cannot delete property '%'"),
    CANNOT_FREEZE("Cannot freeze"),
    CANNOT_FREEZE_ARRAY_BUFFER_VIEW("Cannot freeze array buffer views with elements"),
    CANNOT_PREVENT_EXT("Cannot prevent extensions"),
    CANNOT_READ_PROPERTY("Cannot read property '%' of %"),
    CANNOT_READ_PROPERTIES("Cannot read properties of %"),
    CANNOT_REDEFINE("Cannot redefine property '%'"),
    CANNOT_SEAL("Cannot seal"),
    CANNOT_SET_PROPERTY("Cannot set property '%' of %"),
    CANNOT_SET_PROPERTIES("Cannot set properties of %"),
    CANNOT_WRAP("Cannot wrap target callable (%)"),
    CIRCULAR_STRUCTURE("Converting circular structure to JSON"),
    COLLECTION_MUTATED_WHILE_ITERATION("Collections cannot be mutated during iteration"),
    COMPARATOR_RETURNED_NAN("Return value of compareFn cannot be NaN"),
    CONST_ASSIGN("Assignment to constant variable"),
    CONSTRUCTOR_CLASS_FIELD("Classes may not have a field named 'constructor'"),
    CONSTRUCTOR_NOT_FUNCTION("Constructor % requires 'new'"),
    CONSTRUCTOR_NOT_RECEIVER("The .constructor property is not an object"),
    CURRENCY_CODE("Currency code is required with currency style."),
    CYCLIC_MODULE_DEPENDENCY("Detected cycle while resolving name '%' in '%'"),
    DATA_VIEW_NOT_ARRAY_BUFFER("First argument to DataView constructor must be an ArrayBuffer"),
    DEFINE_DISALLOWED("Cannot define property %, object is not extensible"),
    DEFINE_DISALLOWED_FIXED_LAYOUT("Cannot define property %, object is fixed layout"),
    DETACHED_OPERATION("Cannot perform % on a detached ArrayBuffer"),
    DUPLICATE_TEMPLATE_PROPERTY("Object template has duplicate property '%'"),
    ERROR_NAME_NOT_FOUND("Every classes on inheritance chain of the error are anonymous"),
    EXTENDS_VALUE_NOT_CONSTRUCTOR("Class extends value % is not a constructor or null"),
    FIRST_ARGUMENT_NOT_REG_EXP("First argument to % must not be a regular expression"),
    FUNCTION_BIND("Bind must be called on a function"),
    GENERATOR_RUNNING("Generator is already running"),
    ILLEGAL_INVOCATION("Illegal invocation"),
    IMMUTABLE_PROTOTYPE_SET("Immutable prototype object '%' cannot have their prototype set"),
    IMPORT_ASSERTION_DUPLICATE_KEY("Import assertion has duplicate key '%'"),
    IMPORT_CALL_NOT_NEW_EXPRESSION("Cannot use new with import"),
    IMPORT_OUTSIDE_MODULE("Cannot use import statement outside a module"),
    IMPORT_META_OUTSIDE_MODULE("Cannot use 'import.meta' outside a module"),
    IMPORT_MISSING_SPECIFIER("import() requires a specifier"),
    IMPORT_SHADOW_REALM_REJECTED("Cannot import in ShadowRealm (%)"),
    INCOMPATIBLE_METHOD_RECEIVER("Method % called on incompatible receiver %"),
    INSTANCEOF_NONOBJECT_PROTO("Function has non-object prototype '%' in instanceof check"),
    INSTANCEOF_RHS_IS_NOT_CLASS("Right-hand side of 'instanceof' is not a class"),
    INVALID_ARGUMENT("invalid_argument"),
    INVALID_ARGUMENT_FOR_TEMPORAL("Invalid argument for Temporal %"),
    INVALID_IN_OPERATOR_USE("Cannot use 'in' operator to search for '%' in %"),
    INVALID_PRIVATE_MEMBER_READ("Cannot read private member % from an object whose class did not declare it"),
    INVALID_PRIVATE_MEMBER_WRITE("Cannot write private member % to an object whose class did not declare it"),
    INVALID_PRIVATE_METHOD_WRITE("Private method '%' is not writable"),
    INVALID_PRIVATE_GETTER_ACCESS("'%' was defined without a getter"),
    INVALID_PRIVATE_SETTER_ACCESS("'%' was defined without a setter"),
    INVALID_RAW_JSON_VALUE("Invalid value for JSON.rawJSON"),
    INVALID_REG_EXP_EXEC_RESULT("RegExp exec method returned something other than an Object or null"),
    INVALID_UNIT("Invalid unit argument for %() '%'"),
    ITERABLE_YIELDED_INSUFFICIENT_NUMBER_OF_VALUES("Iterable yielded insufficient number of values (expected: %, actual: %)"),
    ITERABLE_YIELDED_NON_STRING("Iterable yielded % which is not a string"),
    ITERATOR_REDUCE_NO_INITIAL("Reduce of a done iterator with no initial value"),
    ITERATOR_RESULT_NOT_AN_OBJECT("Iterator result % is not an object"),
    ITERATOR_VALUE_NOT_AN_OBJECT("Iterator value % is not an entry object"),
    LANGUAGE_ID("Language ID should be string or object."),
    LHS_RHS_NOT_SAME_TYPE("Left-hand side and right-hand side are not same type"),
    LIST_FORMAT_BAD_PARAMETERS("Incorrect ListFormat information provided"),
    LOCALE_BAD_PARAMETERS("Incorrect locale information provided"),
    LOCALE_NOT_EMPTY("First argument to Intl.Locale constructor can't be empty or missing"),
    MAPPER_FUNCTION_NON_CALLABLE("flatMap mapper function is not callable"),
    METHOD_INVOKED_ON_WRONG_TYPE("Method invoked on an object that is not %."),
    NO_ACCESS("no access"),
    NO_GETTER("Cannot get property % which has only a setter"),
    NO_SETTER("Cannot set property % which has only a getter"),
    NON_CALLABLE_IN_INSTANCE_OF_CHECK("Right-hand side of 'instanceof' is not callable"),
    NON_COERCIBLE("Cannot destructure '%' as it is %."),
    NON_COERCIBLE_WITH_PROPERTY("Cannot destructure property '%' of '%' as it is %."),
    NON_EXTENSIBLE_PROTO("% is not extensible"),
    NON_INTEGER_TO_NON_DECIMAL("Non-integer numbers cannot be converted to non-decimal formatted string"),
    NON_OBJECT_ASSERT_OPTION("The 'assert' option must be an object"),
    NON_OBJECT_IMPORT_ARGUMENT("The second argument to import() must be an object"),
    NON_STRING_IMPORT_ASSERTION_VALUE("Import assertion value must be a string"),
    NOT_AN_ITERATOR("The value is not an iterator because it does not have a method named 'next'"),
    NOT_ASYNC_ITERABLE("% is not async iterable"),
    NOT_CONSTRUCTOR("% is not a constructor"),
    NOT_DATE_OBJECT("this is not a Date object."),
    NOT_GENERIC("% requires that 'this' be a %"),
    NOT_CALLABLE("% is not a function"),
    NOT_CALLABLE_OR_ITERABLE("% is not a function or its return value is not iterable"),
    NOT_CALLABLE_OR_ASYNC_ITERABLE("% is not a function or its return value is not async iterable"),
    NOT_FINITE_NUMBER("Value need to be finite number for %()"),
    NOT_ITERABLE("The value is not iterable because it does not have a method named Symbol.iterator"),
    NOT_ITERABLE_NO_SYMBOL_LOAD("% is not iterable (cannot read property %)"),
    NOT_PROPERTY_NAME("% is not a valid property name"),
    NOT_SUPER_CONSTRUCTOR("Super constructor % of % is not a constructor"),
    OBJECT_GETTER_CALLABLE("Getter must be a function: %"),
    OBJECT_NOT_EXTENSIBLE("Cannot add property %, object is not extensible"),
    OBJECT_SETTER_CALLABLE("Setter must be a function: %"),
    OBJECT_TO_NUMBER("Cannot convert an object to a number"),
    PRIMITIVE_IMMUTABLE("Primitive types are immutable"),
    PRIVATE_NAME_ACCESS_FROM_OUTSIDE("Cannot access private name % from %"),
    PROMISE_CYCLIC("Chaining cycle detected for promise %"),
    PROMISE_EXECUTOR_ALREADY_INVOKED("Promise executor has already been invoked with non-undefined arguments"),
    PROMISE_NON_CALLABLE("Promise resolve or reject function is not callable"),
    PROPERTY_DESC_OBJECT("Property description must be an object: %"),
    PROPERTY_NOT_FUNCTION("'%' returned for property '%' of object '%' is not a function"),
    PROTO_OBJECT_OR_NULL("Object prototype may only be an Object or null: %"),
    PROTOTYPE_PARENT_NOT_AN_OBJECT("Class extends value does not have valid prototype property %"),
    PROXY_CONSTRUCT_NON_OBJECT("'construct' on proxy: trap returned non-object ('%')"),
    PROXY_DEFINE_PROPERTY_NON_CONFIGURABLE("'defineProperty' on proxy: trap returned truish for defining non-configurable property '%' which is either non-existent or configurable in the proxy target"),
    PROXY_DEFINE_PROPERTY_NON_CONFIGURABLE_WRITABLE("'defineProperty' on proxy: trap returned truish for defining non-configurable property '%' which cannot be non-writable, unless there exists a corresponding non-configurable, non-writable own property of the target object."),
    PROXY_DEFINE_PROPERTY_NON_EXTENSIBLE("'defineProperty' on proxy: trap returned truish for adding property '%'  to the non-extensible proxy target"),
    PROXY_DEFINE_PROPERTY_INCOMPATIBLE("'defineProperty' on proxy: trap returned truish for adding property '%'  that is incompatible with the existing property in the proxy target"),
    PROXY_DELETE_PROPERTY_NON_CONFIGURABLE("'deleteProperty' on proxy: trap returned truish for property '%' which is non-configurable in the proxy target"),
    PROXY_DELETE_PROPERTY_NON_EXTENSIBLE("'deleteProperty' on proxy: trap returned truish for property '%' but the proxy target is non-extensible"),
    PROXY_GET_NON_CONFIGURABLE_DATA("'get' on proxy: property '%' is a read-only and non-configurable data property on the proxy target but the proxy did not return its actual value (expected '%' but got '%')"),
    PROXY_GET_NON_CONFIGURABLE_ACCESSOR("'get' on proxy: property '%' is a non-configurable accessor property on the proxy target and does not have a getter function, but the trap did not return 'undefined' (got '%')"),
    PROXY_GET_OWN_PROPERTY_DESCRIPTOR_INCOMPATIBLE("'getOwnPropertyDescriptor' on proxy: trap returned descriptor for property '%' that is incompatible with the existing property in the proxy target"),
    PROXY_GET_OWN_PROPERTY_DESCRIPTOR_INVALID("'getOwnPropertyDescriptor' on proxy: trap returned neither object nor undefined for property '%'"),
    PROXY_GET_OWN_PROPERTY_DESCRIPTOR_NON_CONFIGURABLE("'getOwnPropertyDescriptor' on proxy: trap reported non-configurability for property '%' which is either non-existent or configurable in the proxy target"),
    PROXY_GET_OWN_PROPERTY_DESCRIPTOR_NON_CONFIGURABLE_WRITABLE("'getOwnPropertyDescriptor' on proxy: trap reported non-configurable and writable for property '%' which is non-configurable, non-writable in the proxy target"),
    PROXY_GET_OWN_PROPERTY_DESCRIPTOR_NON_EXTENSIBLE("'getOwnPropertyDescriptor' on proxy: trap returned undefined for property '%' which exists in the non-extensible proxy target"),
    PROXY_GET_OWN_PROPERTY_DESCRIPTOR_UNDEFINED("'getOwnPropertyDescriptor' on proxy: trap returned undefined for property '%' which is non-configurable in the proxy target"),
    PROXY_GET_PROTOTYPE_OF_INVALID("'getPrototypeOf' on proxy: trap returned neither object nor null"),
    PROXY_GET_PROTOTYPE_OF_NON_EXTENSIBLE("'getPrototypeOf' on proxy: proxy target is non-extensible but the trap did not return its actual prototype"),
    PROXY_HAS_NON_CONFIGURABLE("'has' on proxy: trap returned falsish for property '%' which exists in the proxy target as non-configurable"),
    PROXY_HAS_NON_EXTENSIBLE("'has' on proxy: trap returned falsish for property '%' but the proxy target is not extensible"),
    PROXY_IS_EXTENSIBLE_INCONSISTENT("'isExtensible' on proxy: trap result does not reflect extensibility of proxy target (which is '%')"),
    PROXY_NON_OBJECT("Cannot create proxy with a non-object as target or handler"),
    PROXY_OWN_KEYS_MISSING("'ownKeys' on proxy: trap result did not include '%'"),
    PROXY_OWN_KEYS_NON_EXTENSIBLE("'ownKeys' on proxy: trap returned extra keys but proxy target is non-extensible"),
    PROXY_OWN_KEYS_DUPLICATE_ENTRIES("'ownKeys' on proxy: trap returned duplicate entries"),
    PROXY_PREVENT_EXTENSIONS_EXTENSIBLE("'preventExtensions' on proxy: trap returned truish but the proxy target is extensible"),
    PROXY_PRIVATE("Cannot pass private property name to proxy trap"),
    PROXY_REVOKED("Cannot perform '%' on a proxy that has been revoked"),
    PROXY_SET_FROZEN_DATA("'set' on proxy: trap returned truish for property '%' which exists in the proxy target as a non-configurable and non-writable data property with a different value"),
    PROXY_SET_FROZEN_ACCESSOR("'set' on proxy: trap returned truish for property '%' which exists in the proxy target as a non-configurable and non-writable accessor property without a setter"),
    PROXY_SET_PROTOTYPE_OF_NON_EXTENSIBLE("'setPrototypeOf' on proxy: trap returned truish for setting a new prototype on the non-extensible proxy target"),
    PROXY_TRAP_RETURNED_FALSISH("'%' on proxy: trap returned falsish"),
    PROXY_TRAP_RETURNED_FALSISH_FOR("'%' on proxy: trap returned falsish for property '%'"),
    REDUCE_NO_INITIAL("Reduce of empty array with no initial value"),
    REGEXP_FLAGS("Cannot supply flags when constructing one RegExp from another"),
    REGEXP_NON_OBJECT("% getter called on non-object %"),
    REGEXP_NON_REG_EXP("% getter called on non-RegExp object"),
    REGEXP_GLOBAL_CALLED_WITH_NON_GLOBAL("% called with a non-global RegExp argument"),
    REGEXP_NON_GLOBAL_CALLED_WITH_GLOBAL("% called with a global RegExp argument"),
    RELATIVE_DATE_TIME_FORMATTER_BAD_PARAMETERS("Incorrect RelativeDateTimeFormatter provided"),
    REQUIRED_ARGUMENTS_NOT_PROVIDED("Required arguments are not provided (expected: %, actual: %)"),
    REQUIRED_PROPERTY_NOT_FOUND("Property '%' not found, which is required for the destructuring assignment"),
    RESOLVER_NOT_A_FUNCTION("Promise resolver % is not a function"),
    RETURN_METHOD_NOT_CALLABLE("The iterator's 'return' method is not callable"),
    SHARED_ARRAY_BUFFER_TOO_SHORT("Derived SharedArrayBuffer constructor created a buffer which was too small"),
    SHARED_ARRAY_BUFFER_SPECIES_THIS("SharedArrayBuffer subclass returned this from species constructor"),
    STRICT_CANNOT_CREATE_PROPERTY("Cannot create property '%' on % '%'"),
    STRING_MATCH_ALL_NULL_OR_UNDEFINED_FLAGS("The .flags property of the argument to String.prototype.matchAll cannot be null or undefined"),
    SYMBOL_ITERATOR_INVALID("Result of the Symbol.iterator method is not an object"),
    SYMBOL_ASYNC_ITERATOR_INVALID("Result of the Symbol.asyncIterator method is not an object"),
    SYMBOL_KEY_FOR("% is not a symbol"),
    SYMBOL_TO_NUMBER("Cannot convert a symbol to a number"),
    THIS_CANNOT_BE_ACCESSED_IN_GLOBAL("'this' cannot be accessed in global scope"),
    THISARG_NOT_PROVIDED("'this' argument is not provided on a method that depends on 'this'"),
    THROW_METHOD_MISSING("The iterator does not provide a 'throw' method."),
    TOP_LEVEL_AWAIT_STALLED("Top-level await promise never resolved"),
    UNEXPECTED_TYPE("Expected %, but got %"),
}
