package com.avito.android.network_contracts.validation.analyzer

import com.avito.android.network_contracts.validation.diagnostic.NetworkContractsDiagnostic
import com.avito.android.network_contracts.validation.subtractFilesFrom
import java.io.File

internal class NetworkContractsProblemsAnalyzer(
    private val generatedFilesDir: File,
    private val referencesFilesDir: File,
) {

    fun analyze(): List<NetworkContractsDiagnostic> {
        val validationDetections = mutableListOf<NetworkContractsDiagnostic>()

        val changedFiles = generatedFilesDir.subtractFilesFrom(referencesFilesDir)
        if (changedFiles.isNotEmpty()) {
            val corruptedFiles = changedFiles.map(File::getPath)
            validationDetections += NetworkContractsDiagnostic.Failure(corruptedFiles)
        }

        return validationDetections
    }
}
