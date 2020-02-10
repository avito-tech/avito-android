package com.avito.impact.changes

import io.github.azagniotov.matcher.AntPathMatcherArrays

class IgnoreSettings(private val patterns: Set<String> = emptySet()) {

    private val patternMatcher = AntPathMatcherArrays.Builder()
        .withIgnoreCase()
        .withPathSeparator('/')
        .build()

    fun match(path: String): String? {
        return patterns.firstOrNull { pattern ->
            patternMatcher.isMatch(pattern, path)
        }
    }
}
