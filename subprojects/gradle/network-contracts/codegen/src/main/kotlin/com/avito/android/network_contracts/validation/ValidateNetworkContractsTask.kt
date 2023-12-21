package com.avito.android.network_contracts.validation

import com.avito.android.network_contracts.validation.analyzer.NetworkContractsProblemsAnalyzer
import com.avito.android.network_contracts.validation.analyzer.rules.NetworkContractsDiagnosticRule
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

public abstract class ValidateNetworkContractsTask : DefaultTask() {

    @get:OutputFile
    public abstract val resultFile: RegularFileProperty

    internal abstract fun createRules(): List<NetworkContractsDiagnosticRule>

    @TaskAction
    public fun validate() {
        val networkContractsProblemsAnalyzer = NetworkContractsProblemsAnalyzer(createRules())

        val validationDetections = networkContractsProblemsAnalyzer.analyze()
        resultFile.get().asFile.writeText(Json.encodeToString(validationDetections))
    }
}
