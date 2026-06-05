package com.avito.android.string_transform.internal.task.apk

import com.avito.android.Result
import com.avito.android.string_transform.internal.rules.NormalizedRule
import java.io.File
import java.nio.file.Files

internal class WorkspacePathRenamer {

    fun rename(
        workspaceDirectory: File,
        rules: List<NormalizedRule>,
    ): Result<RenameResult> = Result.tryCatch {
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

        val pathMapping = buildPathMapping(workspaceDirectory, validRules)

        workspaceDirectory.walkBottomUp()
            .filter { it != workspaceDirectory }
            .toList()
            .forEach { path ->
                val renamed = transformSegment(path.name, validRules)
                if (renamed != path.name) {
                    val target = path.resolveSibling(renamed)
                    check(!target.exists()) {
                        "Rename target already exists: ${target.path}"
                    }
                    Files.move(path.toPath(), target.toPath())
                }
            }

        RenameResult(warnings = warnings, pathMapping = pathMapping)
    }

    private fun buildPathMapping(
        workspaceDirectory: File,
        validRules: List<NormalizedRule>,
    ): Map<String, String> {
        val mapping = mutableMapOf<String, String>()

        workspaceDirectory.walkBottomUp()
            .filter { it.isFile }
            .toList()
            .forEach { file ->
                val oldRel = file.relativeTo(workspaceDirectory).invariantSeparatorsPath
                val newRel = oldRel.split("/")
                    .joinToString("/") { segment -> transformSegment(segment, validRules) }
                if (newRel != oldRel) {
                    mapping[oldRel] = newRel
                }
            }

        return mapping
    }

    private fun transformSegment(segment: String, rules: List<NormalizedRule>): String {
        return rules.fold(segment) { current, rule ->
            current.replace(rule.from, rule.to)
        }
    }

    private fun isUnsupportedPathCharacter(char: Char): Boolean {
        return char == '/' || char == '\\' || char == '\u0000'
    }
}
