package com.avito.android.build_verdict.internal

import com.avito.utils.getCausesRecursively
import org.gradle.internal.exceptions.MultiCauseException

internal sealed class Error {

    abstract val message: String

    data class Multi(
        override val message: String,
        val errors: List<Single>
    ) : Error()

    data class Single(
        override val message: String,
        val stackTrace: String,
        val causes: List<Cause>
    ) : Error()

    data class Cause(val message: String)

    companion object {
        fun from(throwable: Throwable) = when {
            throwable is MultiCauseException && throwable.causes.size > 1 -> Multi(
                message = throwable.localizedMessage,
                errors = throwable.causes.map { it.toSingle() }
            )
            else -> throwable.toSingle()
        }

        private fun Throwable.toSingle() = Single(
            message = localizedMessage,
            stackTrace = stackTraceToString(),
            causes = getCausesRecursively().map { Cause(it.localizedMessage ?: "${it::class.java} (no error message)") }
        )
    }
}
