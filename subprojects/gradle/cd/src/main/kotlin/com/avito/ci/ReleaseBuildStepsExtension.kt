package com.avito.ci

import com.avito.ci.steps.release.DeployStep
import groovy.lang.Closure
import org.gradle.api.model.ObjectFactory


class ReleaseBuildStepsExtension(
    objects: ObjectFactory
) : BuildStepListExtension("release", objects) {

    fun deploy(closure: Closure<DeployStep>) {
        configureAndAdd(DeployStep(name, artifactsConfig), closure)
    }
}