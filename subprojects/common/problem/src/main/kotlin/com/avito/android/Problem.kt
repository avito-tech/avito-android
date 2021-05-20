package com.avito.android

/**
 * Inspired by https://github.com/melix/jdoctor
 */
public data class Problem(
    val shortDescription: String,
    val context: String,
    val because: String? = null,
    val possibleSolutions: List<String> = emptyList(),
    val documentedAt: String? = null,
    val throwable: Throwable? = null
) {

    public fun newBuilder(): Builder {
        return Builder(this)
    }

    public class Builder(private val shortDescription: String, private val context: String) {

        private var because: String? = null
        private val possibleSolutions = mutableListOf<String>()
        private var documentedAt: String? = null
        private var throwable: Throwable? = null

        private constructor(source: Problem) : this(source.shortDescription, source.context) {
            this.because = source.because
            this.possibleSolutions.addAll(source.possibleSolutions)
            this.documentedAt = source.documentedAt
            this.throwable = source.throwable
        }

        public fun because(reason: String): Builder {
            this.because = reason
            return this
        }

        public fun addSolution(solution: String): Builder {
            this.possibleSolutions.add(solution)
            return this
        }

        public fun documentedAt(link: String): Builder {
            this.documentedAt = link
            return this
        }

        public fun throwable(throwable: Throwable): Builder {
            this.throwable = throwable
            return this
        }

        public fun build(): Problem {
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

public fun Problem.asPlainText(): String = buildString {
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

public fun Problem.asRuntimeException(): RuntimeException = RuntimeException(asPlainText(), throwable)
