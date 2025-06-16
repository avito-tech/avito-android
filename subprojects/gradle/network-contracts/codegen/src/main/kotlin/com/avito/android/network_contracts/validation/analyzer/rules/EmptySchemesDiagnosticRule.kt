package com.avito.android.network_contracts.validation.analyzer.rules

import com.avito.android.network_contracts.validation.analyzer.rules.configurations.EmptySchemesRuleConfiguration

internal class EmptySchemesDiagnosticRule(
    private val configuration: EmptySchemesRuleConfiguration
) : NetworkContractsDiagnosticRule() {

    override fun analyze() {
        if (configuration.schemes.asFileTree.files.isEmpty()) {
            report("Module `${configuration.modulePath.get()}` applies plugin, " +
                "but does not contain any network contracts schemes.")
        }
    }
}
