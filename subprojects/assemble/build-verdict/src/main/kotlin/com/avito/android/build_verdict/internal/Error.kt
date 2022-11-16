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
        private const val noErrorMessage = "(no error message)"

        fun from(throwable: Throwable): Error = when {
            throwable is MultiCauseException && throwable.causes.size > 1 -> Multi(
                message = throwable.getLocalizedMessageSafe(),
                errors = throwable.causes.map { it.toSingle() }
            )

            else -> throwable.toSingle()
        }

        private fun Throwable.toSingle(): Single {
            return Single(
                message = getLocalizedMessageSafe(),
                stackTrace = stackTraceToString(),
                causes = getCausesRecursively().map {
                    Cause(it.getLocalizedMessageSafe())
                }
            )
        }

        private fun Throwable.getLocalizedMessageSafe(): String {
            return localizedMessage ?: "${this::class } $noErrorMessage"
        }
    }
}
