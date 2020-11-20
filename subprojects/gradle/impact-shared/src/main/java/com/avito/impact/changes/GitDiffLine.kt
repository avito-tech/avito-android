package com.avito.impact.changes

import org.funktionale.tries.Try
import java.io.File

internal data class GitDiffLine(val path: String, val changeType: ChangeType)

internal fun GitDiffLine.asChangedFile(rootDir: File): ChangedFile {
    return ChangedFile(rootDir, File(rootDir, path), changeType)
}

/**
 *  Parses line from git diff command.
 *  Examples:
 *  ```
 *  A README.md
 *  R95 old_name.kt new_name.kt
 *  ```
 */
internal fun String.parseGitDiffLine(): Try<GitDiffLine> {
    val parts = this.split(Regex("[ \t]"))
        .map { it.trim() }
    if (parts.size < 2) {
        return Try.Failure(IllegalArgumentException("Line has invalid syntax: $this "))
    }
    val diffTypeCode = extractDiffTypeCode(parts)
    return ChangeType.getTypeByCode(diffTypeCode)
        .flatMap { changeType: ChangeType ->
            if (isSyntaxCorrect(parts, changeType)) {
                Try.Success<GitDiffLine>(GitDiffLine(extractFilePath(parts, changeType), changeType))
            } else {
                Try.Failure<GitDiffLine>(IllegalArgumentException("Line has invalid syntax: $this "))
            }
        }
}

// diff type 'R' and 'C' is followed by probability score e.g. R95, C100 etc
private fun extractDiffTypeCode(parts: List<String>): Char {
    return parts[0].first()
}

@Suppress("MagicNumber")
private fun isSyntaxCorrect(parts: List<String>, changeType: ChangeType): Boolean {
    return when (changeType) {
        ChangeType.ADDED, ChangeType.MODIFIED, ChangeType.DELETED -> parts.size == 2
        ChangeType.COPIED, ChangeType.RENAMED -> parts.size == 3
    }
}

private fun extractFilePath(parts: List<String>, changeType: ChangeType): String {
    return when (changeType) {
        ChangeType.ADDED, ChangeType.MODIFIED, ChangeType.DELETED -> parts[1]
        ChangeType.COPIED, ChangeType.RENAMED -> parts[2]
    }
}
