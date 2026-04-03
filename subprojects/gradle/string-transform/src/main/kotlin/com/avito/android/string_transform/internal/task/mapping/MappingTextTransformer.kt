package com.avito.android.string_transform.internal.task.mapping

import com.avito.android.Result
import com.avito.android.string_transform.internal.rules.NormalizedRule
import java.io.File
import java.nio.charset.StandardCharsets

internal class MappingTextTransformer {

    fun transform(
        inputMapping: File,
        outputMapping: File,
        rules: List<NormalizedRule>,
    ): Result<Unit> = Result.tryCatch {
        val transformed = inputMapping
            .readText(StandardCharsets.UTF_8)
            .let { text -> applyRules(text, rules) }

        outputMapping.writeText(transformed, StandardCharsets.UTF_8)
    }

    internal fun applyRules(
        text: String,
        rules: List<NormalizedRule>,
    ): String {
        return rules.fold(text) { current, rule ->
            current.replace(rule.from, rule.to)
        }
    }
}
