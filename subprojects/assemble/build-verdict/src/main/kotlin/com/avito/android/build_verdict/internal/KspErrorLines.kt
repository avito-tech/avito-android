package com.avito.android.build_verdict.internal

internal class KspErrorLines {

    private var insideError = false

    fun accept(line: String): Boolean {
        when {
            line.startsWith(ERROR_PREFIX) -> insideError = true
            nonErrorLine.containsMatchIn(line) -> insideError = false
        }
        return insideError
    }

    private companion object {
        const val ERROR_PREFIX = "e: [ksp]"
        val nonErrorLine = Regex("^[viw]: \\[ksp]")
    }
}
