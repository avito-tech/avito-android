package com.avito.ci.steps

abstract class SuppressibleBuildStep(context: String, name: String) : BuildStep(context, name) {

    var suppressFailures: Boolean = false
}
