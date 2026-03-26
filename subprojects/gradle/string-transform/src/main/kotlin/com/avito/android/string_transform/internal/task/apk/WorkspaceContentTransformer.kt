package com.avito.android.string_transform.internal.task.apk

import com.avito.android.Result
import com.avito.android.string_transform.internal.rules.NormalizedRule
import java.io.File
import java.nio.charset.StandardCharsets

internal class WorkspaceContentTransformer(
    private val textFileDetector: TextFileDetector,
) {

    fun transform(
        workspaceDirectory: File,
        rules: List<NormalizedRule>,
    ): Result<List<OperationWarning>> = Result.tryCatch {
        val warnings = mutableListOf<OperationWarning>()
        workspaceDirectory.walkTopDown()
            .filter(File::isFile)
            .forEach { file ->
                when (val treatment = classify(file)) {
                    FileTreatment.PlainText ->
                        transformPlainText(file, rules)
                    is FileTreatment.UnsupportedBinary ->
                        warnings += file.unsupportedBinaryWarningIfMatched(
                            workspaceDirectory = workspaceDirectory,
                            rules = rules,
                            reason = treatment.reason,
                        )
                }
            }
        warnings
    }

    private fun transformPlainText(file: File, rules: List<NormalizedRule>) {
        val original = file.readText(StandardCharsets.UTF_8)
        val transformed = rules.fold(original) { content, rule ->
            content.replace(rule.from, rule.to)
        }
        if (transformed != original) {
            file.writeText(transformed, StandardCharsets.UTF_8)
        }
    }

    private fun classify(file: File): FileTreatment {
        return when {
            file.extension == "so" ->
                FileTreatment.UnsupportedBinary(
                    "'.so' files are not supported for content transform yet",
                )
            textFileDetector.isText(file) ->
                FileTreatment.PlainText
            else ->
                FileTreatment.UnsupportedBinary("binary file could not be safely treated as text")
        }
    }

    private fun File.unsupportedBinaryWarningIfMatched(
        workspaceDirectory: File,
        rules: List<NormalizedRule>,
        reason: String,
    ): List<OperationWarning> {
        val matchedLiterals = matchedRuleLiterals(rules)
        if (matchedLiterals.isEmpty()) return emptyList()

        return listOf(
            OperationWarning(
                message = unsupportedBinaryMessage(reason, matchedLiterals),
                affectedPath = relativeTo(workspaceDirectory).invariantSeparatorsPath,
            ),
        )
    }

    private fun unsupportedBinaryMessage(
        reason: String,
        matchedLiterals: List<String>,
    ): String {
        return "Skipped unsupported binary file during content transform " +
            "because $reason. " +
            "Matched literals: ${matchedLiterals.joinToString()}"
    }

    private fun File.matchedRuleLiterals(rules: List<NormalizedRule>): List<String> {
        val bytes = readBytes()
        return rules.asSequence()
            .map(NormalizedRule::from)
            .distinct()
            .filter { literal ->
                bytes.containsSubsequence(literal.toByteArray(StandardCharsets.UTF_8))
            }
            .toList()
    }

    private fun ByteArray.containsSubsequence(needle: ByteArray): Boolean {
        if (needle.isEmpty()) return true
        if (needle.size > size) return false

        val lastStartIndex = size - needle.size
        for (startIndex in 0..lastStartIndex) {
            var matched = true
            for (offset in needle.indices) {
                if (this[startIndex + offset] != needle[offset]) {
                    matched = false
                    break
                }
            }
            if (matched) return true
        }

        return false
    }

    private sealed interface FileTreatment {
        data object PlainText : FileTreatment
        data class UnsupportedBinary(val reason: String) : FileTreatment
    }
}
