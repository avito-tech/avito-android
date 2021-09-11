package com.avito.instrumentation.internal

import com.avito.capitalize
import org.gradle.util.Path

internal object PlanSlugResolver {

    private val symbols = Regex("\\W")

    fun generateDefaultPlanSlug(projectPath: String): String {
        val segments = projectPath.split(Path.SEPARATOR)
        return segments.joinToString(
            separator = "",
            postfix = "Android",
            transform = { removeSymbolsAndCapitalize(it) }
        )
    }

    private fun removeSymbolsAndCapitalize(string: String): String {
        return string.split(symbols)
            .joinToString(
                separator = "",
                transform = { it.capitalize() }
            )
    }
}
