package com.avito.ci.steps

internal interface ImpactAnalysisAwareBuildStep {

    // TODO: rename to skipImpactAnalysis? In most cases we invert the value
    var useImpactAnalysis: Boolean

    class Impl : ImpactAnalysisAwareBuildStep {
        override var useImpactAnalysis: Boolean = false
    }
}
