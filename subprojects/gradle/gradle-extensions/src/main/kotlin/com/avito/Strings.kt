package com.avito

import java.util.Locale

/**
 * replacement for deprecated kotlin's String.capitalize
 */
public fun String.capitalize(): String {
    return replaceFirstChar {
        if (it.isLowerCase()) {
            it.titlecase(Locale.getDefault())
        } else {
            it.toString()
        }
    }
}
