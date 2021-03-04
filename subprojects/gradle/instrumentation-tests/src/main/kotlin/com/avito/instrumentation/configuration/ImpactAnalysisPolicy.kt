package com.avito.instrumentation.configuration

import java.io.Serializable

// todo remove after next release
public sealed class ImpactAnalysisPolicy : Serializable {

    public sealed class On : ImpactAnalysisPolicy() {

        public object RunChangedTests : On()
    }

    public object Off : ImpactAnalysisPolicy()
}
