package com.avito.android

import me.champeau.jdoctor.BaseProblem
import me.champeau.jdoctor.Solution
import java.util.function.Supplier

class SimpleProblem(
    severity: Severity,
    context: ClassContext,
    shortDescription: Supplier<String>?,
    longDescription: Supplier<String>?,
    reason: Supplier<String>?,
    docUrl: Supplier<String>?,
    solutions: List<Supplier<Solution>>
) : BaseProblem<SimpleProblemId, Severity, ClassContext, StubPayload>(
    SimpleProblemId.PROBLEM,
    severity,
    context,
    StubPayload,
    shortDescription,
    longDescription,
    reason,
    docUrl,
    solutions
)
