package com.avito.android.network_contracts.validation.analyzer.rules

import java.io.File

internal class EmptySchemesDiagnosticRule(
    private val modulePath: String,
    private val schemes: Collection<File>,
) : NetworkContractsDiagnosticRule() {

    override fun analyze() {
        if (schemes.isEmpty()) {
            report("Module `$modulePath` applies plugin, but does not contain any network contracts schemes.")
        }
    }
}
