package com.avito.test.summary.analysis

/**
 * Removes all changing data from failure messages to group it afterwards
 */
internal interface FailureMessageNormalizer {

    fun normalize(failureMessage: String): String
}

/**
 * Replaces [regex] with [replacement] in failure message
 *
 * example
 * was   : LinearLayout$LayoutParams@1aeef385
 * become: LinearLayout$LayoutParams
 */
internal class RegexFailureMessageNormalizer(
    private val regex: Regex,
    private val replacement: String = ""
) : FailureMessageNormalizer {

    override fun normalize(failureMessage: String): String = failureMessage.replace(regex, replacement)
}

/**
 * @paran pattern string with {1} {2} ... etc placeholders, will be replaced by [regex] matches one by one
 */
internal class RegexToPatternMessageNormalizer(
    private val regex: Regex,
    private val pattern: String
) : FailureMessageNormalizer {

    override fun normalize(failureMessage: String): String {
        regex.find(failureMessage)?.apply {
            var result = pattern

            groupValues.forEachIndexed { index, match ->
                result = result.replace("{$index}", match.replace("\n", " "))
            }
            return result
        }

        return failureMessage
    }
}

internal object DuplicateFailureMessageNormalizer : FailureMessageNormalizer {

    override fun normalize(failureMessage: String): String {
        val parts = failureMessage.split("\n\n")
        return if (parts.size > 1 && (parts[0].trim() == parts[1].trim())) {
            parts[0]
        } else {
            failureMessage
        }
    }
}
