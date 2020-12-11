package com.avito.android.build_verdict.internal.writer

import com.avito.android.build_verdict.internal.BuildVerdict
import com.avito.android.build_verdict.internal.Error

internal fun BuildVerdict.Configuration.plainText(): String {
    return error.plainText().trimIndent()
}

private fun Error.plainText() = when (this) {
    is Error.Single -> buildString {
        appendln("FAILURE: Build failed with an exception.")
        appendln()
        appendln("* What went wrong:")
        append(plainText())
    }
    is Error.Multi -> plainText()
}

private fun Error.Single.plainText(): String {
    return buildString {
        appendln(message)
        causes.forEachIndexed { index, cause ->
            append("\t".repeat(index + 1))
            append("> ")
            appendln(cause.message.trimIndent())
        }
    }
}

private fun Error.Multi.plainText(): String {
    return buildString {
        appendln("FAILURE: $message")
        appendln()
        errors.forEachIndexed { index, error ->
            appendln("${index + 1}: Task failed with an exception.")
            appendln("-----------")
            appendln(error.plainText().trimIndent())
            if (index < errors.size - 1) {
                appendln()
            }
        }
    }
}
