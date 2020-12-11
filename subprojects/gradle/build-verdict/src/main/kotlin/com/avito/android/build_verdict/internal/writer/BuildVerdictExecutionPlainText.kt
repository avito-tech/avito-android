package com.avito.android.build_verdict.internal.writer

import com.avito.android.build_verdict.internal.BuildVerdict
import com.avito.android.build_verdict.internal.Error
import com.avito.android.build_verdict.internal.FailedTask

internal fun BuildVerdict.Execution.plainText(): String {
    require(failedTasks.isNotEmpty()) {
        "Must have at least one failed task"
    }
    return when (failedTasks.size) {
        1 -> failedTasks[0].plainText()
        else -> buildString {
            appendln("FAILURE: Build completed with ${failedTasks.size} failed tasks.")
            appendln()
            failedTasks.forEachIndexed { index, task ->
                appendln("${index + 1}: ")
                appendln(task.plainText().trimIndent())
            }
        }
    }.trimIndent()
}

private fun FailedTask.plainText() = buildString {
    appendln(error.plainText().trimIndent())
    appendln()
    appendln("* Error logs:")
    appendln(errorOutput.trimIndent())
}

private fun Error.plainText() = buildString {
    appendln("* What went wrong:")
    when (this@plainText) {
        is Error.Single -> appendln(plainText().trimIndent())
        is Error.Multi -> {
            appendln(message.trimIndent())
            errors.forEachIndexed { index, error ->
                append("${1 + index}: ")
                appendln(error.plainText())
                if (index < errors.size - 1) {
                    appendln("==============================================================================")
                }
            }
        }
    }
}

private fun Error.Single.plainText() = buildString {
    appendln(message.trimIndent())
    causes.forEachIndexed { index, cause ->
        append("\t".repeat(index + 1))
        append("> ")
        appendln(cause.message.trimIndent())
    }
}
