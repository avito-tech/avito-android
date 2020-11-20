package com.avito.impact.changes

import org.funktionale.tries.Try

/**
 * @param code code of --diff-filter in git diff
 */
enum class ChangeType(val code: Char) {

    ADDED('A'),
    COPIED('C'),
    DELETED('D'),
    MODIFIED('M'),
    RENAMED('R');

    companion object {
        fun getTypeByCode(code: Char): Try<ChangeType> {
            return values()
                .firstOrNull { it.code == code }
                ?.let { Try.Success(it) }
                ?: Try.Failure(IllegalArgumentException("Cannot parse diff type with code $code"))
        }
    }
}
