package com.avito.instrumentation.configuration

import java.io.Serializable

public sealed class ImpactAnalysisPolicy : Serializable {

    public sealed class On : ImpactAnalysisPolicy() {

        public object RunChangedTests : On()
    }

    public object Off : ImpactAnalysisPolicy()
}
