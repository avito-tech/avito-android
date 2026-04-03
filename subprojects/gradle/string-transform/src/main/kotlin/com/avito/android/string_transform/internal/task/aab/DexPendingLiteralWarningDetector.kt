package com.avito.android.string_transform.internal.task.aab

import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.avito.android.string_transform.internal.task.apk.OperationWarning
import com.avito.android.string_transform.internal.task.apk.containsSubsequence
import java.nio.charset.StandardCharsets

internal fun dexPendingLiteralWarnings(
    outputBytes: ByteArray,
    rules: List<NormalizedRule>,
    affectedPath: String,
): List<OperationWarning> {
    return rules.asSequence()
        .map(NormalizedRule::from)
        .distinct()
        .filter { literal ->
            outputBytes.containsSubsequence(literal.toByteArray(StandardCharsets.UTF_8))
        }
        .map { literal ->
            OperationWarning(
                message = "DEX transform completed, but literal remained after semantic rewrite: '$literal'",
                affectedPath = affectedPath,
            )
        }
        .toList()
}
