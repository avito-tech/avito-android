package com.avito.ci.steps

interface ArtifactAware {

    var artifacts: Set<String>

    class Impl : ArtifactAware {

        override var artifacts: Set<String> = mutableSetOf()
    }
}
