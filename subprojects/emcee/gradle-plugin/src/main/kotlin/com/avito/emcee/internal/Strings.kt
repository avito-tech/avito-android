package com.avito.emcee.internal

import java.util.Locale

/**
 * replacement for deprecated kotlin's String.capitalize
 *
 * https://kotlinlang.org/docs/whatsnew15.html#stable-locale-agnostic-api-for-upper-lowercasing-text
 */
internal fun String.capitalize(): String {
    return replaceFirstChar {
        if (it.isLowerCase()) {
            it.titlecase(Locale.getDefault())
        } else {
            it.toString()
        }
    }
}
