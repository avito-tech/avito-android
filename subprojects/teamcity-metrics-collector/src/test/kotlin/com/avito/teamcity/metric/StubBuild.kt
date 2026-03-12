package com.avito.teamcity.metric

import org.jetbrains.teamcity.rest.Branch
import org.jetbrains.teamcity.rest.Build
import org.jetbrains.teamcity.rest.BuildAgent
import org.jetbrains.teamcity.rest.BuildArtifact
import org.jetbrains.teamcity.rest.BuildCanceledInfo
import org.jetbrains.teamcity.rest.BuildCommentInfo
import org.jetbrains.teamcity.rest.BuildConfigurationId
import org.jetbrains.teamcity.rest.BuildId
import org.jetbrains.teamcity.rest.BuildProblemOccurrence
import org.jetbrains.teamcity.rest.BuildRunningInfo
import org.jetbrains.teamcity.rest.BuildState
import org.jetbrains.teamcity.rest.BuildStatus
import org.jetbrains.teamcity.rest.Change
import org.jetbrains.teamcity.rest.Parameter
import org.jetbrains.teamcity.rest.PinInfo
import org.jetbrains.teamcity.rest.ProjectId
import org.jetbrains.teamcity.rest.Property
import org.jetbrains.teamcity.rest.Revision
import org.jetbrains.teamcity.rest.TestRunsLocator
import org.jetbrains.teamcity.rest.TestStatus
import org.jetbrains.teamcity.rest.TriggeredInfo
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.time.ZonedDateTime
import java.util.Date

internal class StubParameter(
    override val name: String,
    override val value: String?,
    override val own: Boolean = false,
) : Parameter

@Suppress("OVERRIDE_DEPRECATION")
internal class StubBuild(
    private val resultingParameters: List<Parameter> = emptyList(),
) : Build {
    override val id: BuildId get() = BuildId("1")
    override val buildConfigurationId: BuildConfigurationId get() = BuildConfigurationId("TestBuild")
    override val buildNumber: String get() = "1"
    override val status: BuildStatus get() = BuildStatus.SUCCESS
    override val branch: Branch get() = object : Branch {
        override val name: String? get() = "main"
        override val isDefault: Boolean get() = true
    }
    override val state: BuildState get() = BuildState.FINISHED
    override val personal: Boolean get() = false
    override val name: String get() = "test"
    override val projectName: String get() = "TestProject"
    override val canceledInfo: BuildCanceledInfo? get() = null
    override val comment: BuildCommentInfo? get() = null
    override val composite: Boolean? get() = false
    override val statusText: String? get() = null
    override val parameters: List<Parameter> get() = emptyList()
    override val tags: List<String> get() = emptyList()
    override val revisions: List<Revision> get() = emptyList()
    override val changes: List<Change> get() = emptyList()
    override val snapshotDependencies: List<Build> get() = emptyList()
    override val pinInfo: PinInfo? get() = null
    override val triggeredInfo: TriggeredInfo? get() = null
    override val agent: BuildAgent? get() = null
    override val detachedFromAgent: Boolean get() = false
    override val statistics: List<Property> get() = emptyList()
    override val queuedWaitReasons: List<Property> get() = emptyList()
    override val projectId: ProjectId get() = ProjectId("TestProject")
    override val buildProblems: Sequence<BuildProblemOccurrence> get() = emptySequence()
    override val isHistory: Boolean get() = false
    override val isFailedToStart: Boolean get() = false

    private val start: ZonedDateTime = ZonedDateTime.parse("2026-02-06T09:00:00+03:00")
    override val startDateTime: ZonedDateTime get() = start
    override val finishDateTime: ZonedDateTime get() = start.plusMinutes(10)
    override val queuedDateTime: ZonedDateTime get() = start.minusSeconds(5)
    override val runningInfo: BuildRunningInfo? get() = null

    override val buildTypeId: BuildConfigurationId get() = buildConfigurationId
    override val finishDate: Date? get() = null
    override val startDate: Date? get() = null
    override val queuedDate: Date? get() = null

    override fun getResultingParameters(): List<Parameter> = resultingParameters
    override fun addTag(tag: String) {}
    override fun setComment(comment: String) {}
    override fun replaceTags(tags: List<String>) {}
    override fun pin(comment: String) {}
    override fun unpin(comment: String) {}
    override fun getArtifacts(
        parentPath: String,
        recursive: Boolean,
        hidden: Boolean,
    ): List<BuildArtifact> = emptyList()

    override fun findArtifact(
        pattern: String,
        parentPath: String,
    ): BuildArtifact = throw UnsupportedOperationException()

    override fun findArtifact(
        pattern: String,
        parentPath: String,
        recursive: Boolean,
    ): BuildArtifact = throw UnsupportedOperationException()
    override fun downloadArtifacts(pattern: String, outputDir: File) {}
    override fun downloadArtifact(artifactPath: String, output: OutputStream) {}
    override fun downloadArtifact(artifactPath: String, output: File) {}
    override fun openArtifactInputStream(artifactPath: String): InputStream = InputStream.nullInputStream()
    override fun downloadBuildLog(output: File) {}
    override fun cancel(comment: String, reAddIntoQueue: Boolean) {}
    override fun finish() {}
    override fun log(message: String) {}
    override fun markAsSuccessful(comment: String) {}
    override fun markAsFailed(comment: String) {}
    @Suppress("DEPRECATION")
    override fun tests(status: TestStatus?): Sequence<org.jetbrains.teamcity.rest.TestOccurrence> = emptySequence()
    override fun testRunsLocator(status: TestStatus?): TestRunsLocator = throw UnsupportedOperationException()
    override fun testRuns(status: TestStatus?): Sequence<org.jetbrains.teamcity.rest.TestRun> = emptySequence()

    override fun getHomeUrl(): String = ""
    override fun getWebUrl(): String = ""
    override fun fetchStatusText(): String = ""
    override fun fetchQueuedDate(): Date? = null
    override fun fetchStartDate(): Date? = null
    override fun fetchFinishDate(): Date? = null
    override fun fetchParameters(): List<Parameter> = emptyList()
    override fun fetchRevisions(): List<Revision> = emptyList()
    override fun fetchChanges(): List<Change> = emptyList()
    override fun fetchPinInfo(): PinInfo? = null
    override fun fetchTriggeredInfo(): TriggeredInfo? = null
}
