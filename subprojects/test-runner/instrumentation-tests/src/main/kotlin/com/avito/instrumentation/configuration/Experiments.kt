package com.avito.instrumentation.configuration

import java.io.Serializable

public data class Experiments(
    val saveTestArtifactsToOutputs: Boolean,
    val fetchLogcatForIncompleteTests: Boolean,
    val uploadArtifactsFromRunner: Boolean,
) : Serializable
