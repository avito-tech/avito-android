package com.avito.android.contracts.platform.scheme.validation.analyzer.rules

import com.avito.android.contracts.platform.scheme.validation.analyzer.diagnostic.NetworkContractsDiagnostic
import com.avito.android.contracts.platform.scheme.validation.analyzer.diagnostic.NetworkContractsIssue
import org.gradle.api.Named
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

public abstract class NetworkContractsDiagnosticRule : Named {

    @get: Internal
    public open val issue: NetworkContractsIssue = NetworkContractsIssue(
        key = javaClass.simpleName
    )

    @get: Internal
    public val findings: List<NetworkContractsDiagnostic>
        get() = internalFindings.toList()

    private val internalFindings: MutableList<NetworkContractsDiagnostic> = mutableListOf()

    @Input
    abstract override fun getName(): String

    public abstract fun analyze()

    protected fun report(diagnostic: NetworkContractsDiagnostic) {
        internalFindings.add(diagnostic)
    }
}
