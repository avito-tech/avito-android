package com.avito.android

import org.apache.commons.configuration.PropertiesConfiguration

internal fun generateComment(commonComment: String?): String {
    return buildString {
        append("from common properties")
        if (!commonComment.isNullOrBlank()) {
            appendLine()
            append(commonComment)
        }
    }
}

internal fun PropertiesConfiguration.getRawComment(key: String): String? {
    return layout.getCanonicalComment(key, false)
}
