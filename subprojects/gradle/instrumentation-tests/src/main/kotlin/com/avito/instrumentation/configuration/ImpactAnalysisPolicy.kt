package com.avito.instrumentation.configuration

import java.io.Serializable

sealed class ImpactAnalysisPolicy : Serializable {

    sealed class On : ImpactAnalysisPolicy() {

        object RunAffectedTests : On()

        // Use RunChangedTests, it also includes new tests
        object RunNewTests : On()

        // Use RunChangedTests, it also includes modified tests
        object RunModifiedTests : On()

        object RunChangedTests : On()
    }

    object Off : ImpactAnalysisPolicy()
}
