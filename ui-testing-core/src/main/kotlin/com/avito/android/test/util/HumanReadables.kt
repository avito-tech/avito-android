package com.avito.android.test.util

import android.content.res.Resources
import android.view.View
import android.widget.TextView

/**
 * Dead simple string representation of any [View]
 */
internal fun View?.describe(): String {
    if (this == null) return "null"

    val result = mutableListOf<String>()

    try {
        result += "id=${resources?.getResourceEntryName(id)}"
    } catch (e: Resources.NotFoundException) {
        // do nothing
    }

    if (!contentDescription.isNullOrBlank()) {
        result += "desc=$contentDescription"
    }

    if (this is TextView) {

        if (!text.isNullOrBlank()) {
            result += "text=$text"
        }

        if (!hint.isNullOrBlank()) {
            result += "hint=$hint"
        }
    }

    return "${this::class.java.simpleName}(${result.joinToString(separator = ";")})"
}
