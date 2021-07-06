package com.avito.cd

public data class CdBuildResult(
    val schemaVersion: Long,
    val teamcityBuildUrl: String,
    val buildNumber: String,
    val releaseVersion: String,
    val gitBranch: GitBranch,
    val testResults: TestResultsLink,
    val artifacts: List<Artifact>
) {

    public data class GitBranch(
        val name: String,
        val commitHash: String
    )

    public data class TestResultsLink(
        val reportUrl: String,
        val reportCoordinates: ReportCoordinates
    ) {
        public data class ReportCoordinates(
            val planSlug: String,
            val jobSlug: String,
            val runId: String
        )
    }

    public sealed class Artifact {
        public abstract val type: String
        public abstract val name: String
        public abstract val uri: String

        public data class AndroidBinary(
            override val type: String,
            override val name: String,
            override val uri: String,
            val buildVariant: BuildVariant
        ) : Artifact()

        public data class FileArtifact(
            override val type: String,
            override val name: String,
            override val uri: String
        ) : Artifact()
    }
}
