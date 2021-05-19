package com.avito.instrumentation.internal.report.listener

/**
 * Inspired by https://github.com/melix/jdoctor
 */
internal data class Problem(
    val shortDescription: String,
    val context: String,
    val because: String? = null,
    val possibleSolutions: List<String> = emptyList(),
    val documentedAt: String? = null,
    val throwable: Throwable? = null
) {

    fun newBuilder(): Builder {
        return Builder(this)
    }

    class Builder(private val shortDescription: String, private val context: String) {

        private var because: String? = null
        private val possibleSolutions = mutableListOf<String>()
        private var documentedAt: String? = null
        private var throwable: Throwable? = null

        constructor(source: Problem) : this(source.shortDescription, source.context) {
            this.because = source.because
            this.possibleSolutions.addAll(source.possibleSolutions)
            this.documentedAt = source.documentedAt
            this.throwable = source.throwable
        }

        fun because(reason: String): Builder {
            this.because = reason
            return this
        }

        fun addSolution(solution: String): Builder {
            this.possibleSolutions.add(solution)
            return this
        }

        fun documentedAt(link: String): Builder {
            this.documentedAt = link
            return this
        }

        fun throwable(throwable: Throwable): Builder {
            this.throwable = throwable
            return this
        }

        fun build(): Problem {
            return Problem(
                shortDescription = this.shortDescription,
                context = this.context,
                because = because,
                possibleSolutions = possibleSolutions,
                documentedAt = documentedAt,
                throwable = throwable,
            )
        }
    }
}

internal fun Problem.asPlainText(): String = buildString {
    appendLine(shortDescription)
    appendLine("Where : $context")
    appendLine("Why? : $because")
    if (possibleSolutions.isNotEmpty()) {
        appendLine("Possible solutions:")
        possibleSolutions.forEach {
            appendLine(" - $it")
        }
    }
    if (!documentedAt.isNullOrBlank()) {
        appendLine("You can learn more about this problem at $documentedAt")
    }

    if (throwable != null && !throwable.message.isNullOrBlank()) {
        appendLine("Cause exception message: ${throwable.message}")
    }
}

internal fun Problem.asRuntimeException(): RuntimeException = RuntimeException(asPlainText(), throwable)
