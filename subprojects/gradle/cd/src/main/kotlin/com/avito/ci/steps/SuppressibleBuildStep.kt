package com.avito.ci.steps

abstract class SuppressibleBuildStep(context: String) : BuildStep(context) {

    var suppressFailures: Boolean = false

}
