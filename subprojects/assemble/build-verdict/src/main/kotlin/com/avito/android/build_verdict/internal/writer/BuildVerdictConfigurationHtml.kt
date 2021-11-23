package com.avito.android.build_verdict.internal.writer

import com.avito.android.build_verdict.internal.BuildVerdict
import com.avito.android.build_verdict.internal.Error
import kotlinx.html.body
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.pre
import kotlinx.html.stream.createHTML
import kotlinx.html.title

internal fun BuildVerdict.Configuration.html(): String {
    return error.html().trimIndent()
}

private fun Error.html() = createHTML().html {
    head {
        title {
            text("Build failed")
        }
    }
    body {
        when (this@html) {
            is Error.Single -> {
                h2 {
                    text("FAILURE: Build failed with an exception")
                }
                h3 {
                    text("What went wrong:")
                }
                pre {
                    text(text())
                }
            }
            is Error.Multi -> {
                h2 {
                    text("FAILURE: $message")
                }
                errors.forEachIndexed { index, error ->
                    h3 {
                        text("${index + 1}: Task failed with an exception")
                    }
                    pre {
                        text(error.text().trimIndent())
                    }
                }
            }
        }
    }
}

private fun Error.Single.text(): String {
    return buildString {
        appendLine(message)
        causes.forEachIndexed { index, cause ->
            append("\t".repeat(index + 1))
            append("> ")
            appendLine(cause.message.trimIndent())
        }
    }
}
