package com.avito.ci.steps

public abstract class SuppressibleBuildStep(context: String, name: String) : BuildStep(context, name) {

    public var suppressFailures: Boolean = false
}
