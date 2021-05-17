package com.avito.android

import me.champeau.jdoctor.Problem
import me.champeau.jdoctor.Solution
import me.champeau.jdoctor.builders.Builder
import me.champeau.jdoctor.builders.ProblemBuilder
import me.champeau.jdoctor.builders.SolutionBuilder
import java.util.function.Consumer
import java.util.function.Supplier

class SimpleProblemBuilder(
    private val severity: Severity,
    private val context: ClassContext,
) : ProblemBuilder<SimpleProblemId, Severity, ClassContext, StubPayload>,
    Supplier<Problem<SimpleProblemId, Severity, ClassContext, StubPayload>> {

    private var shortDescription: Supplier<String>? = null
    private var longDescription: Supplier<String>? = null
    private var docUrl: Supplier<String>? = null
    private var reason: Supplier<String>? = null
    private val solutions = mutableListOf<Supplier<Solution>>()

    override fun withShortDescription(
        shortDescription: Supplier<String>?
    ): ProblemBuilder<SimpleProblemId, Severity, ClassContext, StubPayload> {
        this.shortDescription = shortDescription
        return this
    }

    override fun withLongDescription(
        longDescription: Supplier<String>?
    ): ProblemBuilder<SimpleProblemId, Severity, ClassContext, StubPayload> {
        this.longDescription = longDescription
        return this
    }

    override fun documentedAt(
        link: Supplier<String>?
    ): ProblemBuilder<SimpleProblemId, Severity, ClassContext, StubPayload> {
        this.docUrl = link
        return this
    }

    override fun addSolution(
        solutionSpec: Consumer<in SolutionBuilder>?
    ): ProblemBuilder<SimpleProblemId, Severity, ClassContext, StubPayload> {
        if (solutionSpec != null) {
            this.solutions.add {
                val builder = SolutionBuilder.newSolution()
                solutionSpec.accept(builder)
                Builder.build(builder)
            }
        }
        return this
    }

    override fun because(
        reason: Supplier<String>?
    ): ProblemBuilder<SimpleProblemId, Severity, ClassContext, StubPayload> {
        this.reason = reason
        return this
    }

    override fun get(): Problem<SimpleProblemId, Severity, ClassContext, StubPayload> {
        return SimpleProblem(
            severity = severity,
            context = context,
            shortDescription = shortDescription,
            longDescription = longDescription,
            reason = reason,
            docUrl = docUrl,
            solutions = solutions
        )
    }

    companion object {

        fun newBuilder(
            severity: Severity,
            context: ClassContext
        ): SimpleProblemBuilder {
            return SimpleProblemBuilder(
                severity = severity,
                context = context
            )
        }
    }
}
