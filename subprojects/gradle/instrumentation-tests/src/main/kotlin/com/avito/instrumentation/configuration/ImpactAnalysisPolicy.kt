package com.avito.instrumentation.configuration

import java.io.Serializable

sealed class ImpactAnalysisPolicy : Serializable {

    sealed class On : ImpactAnalysisPolicy() {

        object RunAffectedTests : On()

        // Use RunModifiedTests, it also includes new tests
        object RunNewTests : On()

        object RunModifiedTests : On()
    }

    object Off : ImpactAnalysisPolicy()
}
