package com.avito.android.string_transform.internal.task.apk

import com.avito.android.Result
import com.avito.android.string_transform.internal.rules.NormalizedRule
import java.io.File
import java.nio.file.Files

internal class WorkspacePathRenamer {

    fun rename(
        workspaceDirectory: File,
        rules: List<NormalizedRule>,
    ): Result<List<OperationWarning>> = Result.tryCatch {
        val warnings = mutableListOf<OperationWarning>()
        val validRules = rules.filter { rule ->
            val isValid = rule.to.none(::isUnsupportedPathCharacter)
            if (!isValid) {
                warnings += OperationWarning(
                    message = "Rename rule '${rule.from}' -> '${rule.to}' is ignored for paths " +
                        "because target contains unsupported filename characters.",
                )
            }
            isValid
        }

        workspaceDirectory.walkBottomUp()
            .filter { it != workspaceDirectory }
            .toList()
            .forEach { path ->
                val renamed = validRules.fold(path.name) { current, rule ->
                    current.replace(rule.from, rule.to)
                }
                if (renamed != path.name) {
                    val target = path.resolveSibling(renamed)
                    check(!target.exists()) {
                        "Rename target already exists: ${target.path}"
                    }
                    Files.move(path.toPath(), target.toPath())
                }
            }

        warnings
    }

    private fun isUnsupportedPathCharacter(char: Char): Boolean {
        return char == '/' || char == '\\' || char == '\u0000'
    }
}
