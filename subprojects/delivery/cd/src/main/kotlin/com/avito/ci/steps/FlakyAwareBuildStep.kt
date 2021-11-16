package com.avito.ci.steps

internal interface FlakyAwareBuildStep {

    /**
     * Whether skip results of tests marked with @Flaky or not
     */
    var suppressFlaky: Boolean

    class Impl : FlakyAwareBuildStep {

        override var suppressFlaky: Boolean = false
    }
}
