package com.avito.instrumentation.configuration

import java.io.Serializable

sealed class ImpactAnalysisPolicy : Serializable {

    sealed class On : ImpactAnalysisPolicy() {

        object RunChangedTests : On()
    }

    object Off : ImpactAnalysisPolicy()
}
