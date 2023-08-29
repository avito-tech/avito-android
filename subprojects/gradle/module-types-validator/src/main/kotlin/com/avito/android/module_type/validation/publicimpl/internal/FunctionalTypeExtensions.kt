package com.avito.android.module_type.validation.publicimpl.internal

import com.avito.android.module_type.FunctionalType

/**
 * Provide a regex from an array of functional types in the form of all possible variants.
 * For example, a regex would be of the form:
 *
 * `^(public|impl)(-.+)?$`.
 *
 * This is needs for finding a functional type from the string representation of a module like: `:lib-a:impl` etc.
 */
internal fun Array<FunctionalType>.asRegex(): Regex {
    val types = joinToString(separator = "|") {
        Regex.escape(it.name.lowercase())
    }
    return Regex("^($types)(-.+)?$")
}

internal fun Collection<FunctionalType>.asRegex(): Regex {
    return toTypedArray().asRegex()
}
