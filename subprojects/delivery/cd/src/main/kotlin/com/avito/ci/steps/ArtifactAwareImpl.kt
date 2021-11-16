package com.avito.ci.steps

internal class ArtifactAwareImpl : ArtifactAware {

    override var artifacts: Set<String> = mutableSetOf()
}
