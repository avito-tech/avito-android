package com.avito.impact.configuration

import io.github.azagniotov.matcher.AntPathMatcher

internal fun isBranchProtected(branch: String, protectedBranches: Set<String>): Boolean {
    require(branch.isNotBlank()) { "trying to check empty branch" }
    return if (protectedBranches.isEmpty()) {
        false
    } else {
        protectedBranches.any { pattern ->

            require(pattern.isNotBlank()) { "protectedBranchesPattern must not be empty string" }

            val patternMatcher = AntPathMatcher.Builder()
                .withIgnoreCase()
                .withPathSeparator('/')
                .build()

            patternMatcher.isMatch(pattern, branch)
        }
    }
}
