package com.avito.cd

class BuildOutput {
    val testResults = mutableMapOf<String, CdBuildResult.TestResults>()
    var artifacts = emptyList<CdBuildResult.Artifact>()
}
