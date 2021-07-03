package com.avito.cd

public class BuildOutput {
    public val testResults: MutableMap<String, CdBuildResult.TestResultsLink> = mutableMapOf()
    public var artifacts: List<CdBuildResult.Artifact> = emptyList()
}
