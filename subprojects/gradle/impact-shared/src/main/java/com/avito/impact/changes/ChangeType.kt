package com.avito.impact.changes

import com.avito.android.Result

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

        fun getTypeByCode(code: Char): Result<ChangeType> {
            return values()
                .firstOrNull { it.code == code }
                ?.let { Result.Success(it) }
                ?: Result.Failure(IllegalArgumentException("Cannot parse diff type with code $code"))
        }
    }
}
