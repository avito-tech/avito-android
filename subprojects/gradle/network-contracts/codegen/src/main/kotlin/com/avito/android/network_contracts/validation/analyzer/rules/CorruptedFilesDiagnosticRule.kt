package com.avito.android.network_contracts.validation.analyzer.rules

import com.avito.android.network_contracts.validation.subtractFilesFrom
import java.io.File

internal class CorruptedFilesDiagnosticRule(
    private val generatedFilesDir: File,
    private val referencesFilesDir: File,
) : NetworkContractsDiagnosticRule() {

    override fun analyze() {
        generatedFilesDir.subtractFilesFrom(referencesFilesDir)
            .forEach { corruptedFile ->
                report("Found corrupted file://${corruptedFile.path}")
            }
    }
}
