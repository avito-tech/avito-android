package com.avito.android.build_verdict.internal.writer

import com.avito.android.build_verdict.internal.BuildVerdict
import com.avito.android.build_verdict.internal.Error
import com.avito.android.build_verdict.internal.FailedTask
import kotlinx.html.BODY
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.h4
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.pre
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe

internal fun BuildVerdict.Execution.html(): String {
    require(failedTasks.isNotEmpty()) {
        "Must have at least one failed task"
    }
    return createHTML().html {
        head {
            title {
                text("BuildFailed")
            }
            style {
                unsafe {
                    raw(
                        """
.logs {
    color: red;
}""".trimIndent()
                    )
                }
            }
        }
        body {
            when (failedTasks.size) {
                1 -> task(task = failedTasks[0])
                else -> {
                    h1 {
                        text("FAILURE: Build completed with ${failedTasks.size} failed tasks")
                    }
                    failedTasks.forEachIndexed { index, task ->
                        task(titlePostfix = "${index + 1}: ", task = task)
                    }
                }
            }
        }
    }
}

private fun BODY.task(titlePostfix: String = "", task: FailedTask) {
    h2 {
        text("${titlePostfix}What went wrong:")
    }
    error(task.error)
    if (task.verdict != null && !task.verdict.isEmpty()) {
        h3 {
            text("Task verdict:")
        }
        pre {
            unsafe {
                raw(task.verdict.html.trimIndent())
            }
        }
    }
    h3 {
        text("Error logs:")
    }
    pre(classes = "logs") {
        text(task.errorLogs.trimIndent())
    }
}

private fun BODY.error(error: Error) {
    when (error) {
        is Error.Single -> pre {
            text(error.text())
        }
        is Error.Multi -> {
            h4 {
                text(error.message.trimIndent())
            }
            val size = error.errors.size
            error.errors.forEachIndexed { index, childError ->
                pre {
                    text("${1 + index}: ${childError.text()}")
                }
                if (index < size - 1) {
                    br()
                }
            }
        }
    }
}

private fun Error.Single.text() = buildString {
    appendLine(message.trimIndent())
    causes.forEachIndexed { index, cause ->
        append("\t".repeat(index + 1))
        append("> ")
        appendLine(cause.message.trimIndent())
    }
}
