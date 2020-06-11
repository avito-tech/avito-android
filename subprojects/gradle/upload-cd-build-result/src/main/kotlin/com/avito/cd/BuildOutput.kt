package com.avito.cd

class BuildOutput {
    val testResults = mutableMapOf<String, CdBuildResult.TestResultsLink>()
    var artifacts = emptyList<CdBuildResult.Artifact>()
}
