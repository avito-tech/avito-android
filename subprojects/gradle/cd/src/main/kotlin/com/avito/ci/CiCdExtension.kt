package com.avito.ci

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory

@Suppress("UnstableApiUsage")
open class CiCdExtension(objects: ObjectFactory) {

    internal val localCheckSteps = BuildStepListExtension("localCheck", objects)

    internal val releaseSteps = BuildStepListExtension("release", objects)

    internal val fullCheckSteps = BuildStepListExtension("fullCheck", objects)

    internal val fastCheckSteps = BuildStepListExtension("fastCheck", objects).apply {
        useImpactAnalysis = true
    }

    fun localCheck(action: Action<BuildStepListExtension>) {
        action.execute(localCheckSteps)
    }

    fun release(action: Action<BuildStepListExtension>) {
        action.execute(releaseSteps)
    }

    fun fullCheck(action: Action<BuildStepListExtension>) {
        action.execute(fullCheckSteps)
    }

    fun fastCheck(action: Action<BuildStepListExtension>) {
        action.execute(fastCheckSteps)
    }
}
