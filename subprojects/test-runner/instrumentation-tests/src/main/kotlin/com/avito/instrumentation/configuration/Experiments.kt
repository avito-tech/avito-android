package com.avito.instrumentation.configuration

import java.io.Serializable

public data class Experiments(
    val saveTestArtifactsToOutputs: Boolean,
    val uploadArtifactsFromRunner: Boolean,
    val useLegacyExtensionsV1Beta: Boolean,
    val sendPodsMetrics: Boolean,
) : Serializable
