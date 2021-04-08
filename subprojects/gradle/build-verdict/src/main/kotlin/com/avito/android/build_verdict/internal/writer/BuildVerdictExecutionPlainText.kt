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
            appendLine("FAILURE: Build completed with ${failedTasks.size} failed tasks.")
            appendLine()
            failedTasks.forEachIndexed { index, task ->
                appendLine("${index + 1}: ")
                appendLine(task.plainText().trimIndent())
            }
        }
    }.trimIndent()
}

private fun FailedTask.plainText() = buildString {
    appendLine(error.plainText().trimIndent())
    appendLine()
    if (verdict != null && !verdict.isEmpty()) {
        appendLine("* Task result:")
        appendLine(verdict.plain.trimIndent())
        appendLine()
    }
    appendLine("* Error logs:")
    appendLine(errorLogs.trimIndent())
}

private fun Error.plainText() = buildString {
    appendLine("* What went wrong:")
    when (this@plainText) {
        is Error.Single -> appendLine(plainText().trimIndent())
        is Error.Multi -> {
            appendLine(message.trimIndent())
            errors.forEachIndexed { index, error ->
                append("${1 + index}: ")
                appendLine(error.plainText())
                if (index < errors.size - 1) {
                    appendLine("==============================================================================")
                }
            }
        }
    }
}

private fun Error.Single.plainText() = buildString {
    appendLine(message.trimIndent())
    causes.forEachIndexed { index, cause ->
        append("\t".repeat(index + 1))
        append("> ")
        appendLine(cause.message.trimIndent())
    }
}
