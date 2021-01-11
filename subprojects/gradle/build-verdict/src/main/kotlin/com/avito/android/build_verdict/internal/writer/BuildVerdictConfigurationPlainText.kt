package com.avito.android.build_verdict.internal.writer

import com.avito.android.build_verdict.internal.BuildVerdict
import com.avito.android.build_verdict.internal.Error

internal fun BuildVerdict.Configuration.plainText(): String {
    return error.plainText().trimIndent()
}

private fun Error.plainText() = when (this) {
    is Error.Single -> buildString {
        appendLine("FAILURE: Build failed with an exception.")
        appendLine()
        appendLine("* What went wrong:")
        append(plainText())
    }
    is Error.Multi -> plainText()
}

private fun Error.Single.plainText(): String {
    return buildString {
        appendLine(message)
        causes.forEachIndexed { index, cause ->
            append("\t".repeat(index + 1))
            append("> ")
            appendLine(cause.message.trimIndent())
        }
    }
}

private fun Error.Multi.plainText(): String {
    return buildString {
        appendLine("FAILURE: $message")
        appendLine()
        errors.forEachIndexed { index, error ->
            appendLine("${index + 1}: Task failed with an exception.")
            appendLine("-----------")
            appendLine(error.plainText().trimIndent())
            if (index < errors.size - 1) {
                appendLine()
            }
        }
    }
}
