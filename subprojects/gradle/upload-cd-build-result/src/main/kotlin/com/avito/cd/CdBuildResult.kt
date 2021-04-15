package com.avito.cd

data class CdBuildResult(
    val schemaVersion: Long,
    val teamcityBuildUrl: String,
    val buildNumber: String,
    val releaseVersion: String,
    val gitBranch: GitBranch,
    val testResults: TestResultsLink,
    val artifacts: List<Artifact>
) {
    data class GitBranch(
        val name: String,
        val commitHash: String
    )

    data class TestResultsLink(
        val reportUrl: String,
        val reportCoordinates: ReportCoordinates
    ) {
        // TODO дублирование модели из report-viewer
        data class ReportCoordinates(
            val planSlug: String,
            val jobSlug: String,
            val runId: String
        )
    }

    sealed class Artifact {
        abstract val type: String
        abstract val name: String
        abstract val uri: String

        data class AndroidBinary(
            override val type: String,
            override val name: String,
            override val uri: String,
            val buildVariant: BuildVariant
        ) : Artifact()

        data class FileArtifact(
            override val type: String,
            override val name: String,
            override val uri: String
        ) : Artifact()
    }
}
