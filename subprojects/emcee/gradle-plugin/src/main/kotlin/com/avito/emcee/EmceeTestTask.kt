package com.avito.emcee

import com.avito.emcee.internal.ArtifactorySettings
import com.avito.emcee.internal.EmceeConfigTestHelper
import com.avito.emcee.internal.getApkOrThrow
import org.gradle.api.DefaultTask
import com.avito.emcee.queue.DeviceConfiguration
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.time.Duration
import kotlin.time.ExperimentalTime

public abstract class EmceeTestTask : DefaultTask() {

    @get:Nested
    public abstract val job: Property<JobConfiguration>

    @get:Nested
    public abstract val artifactory: Property<ArtifactoryConfiguration>

    @get:Input
    public abstract val retries: Property<Int>

    @get:Input
    public abstract val deviceApis: ListProperty<Int>

    @get:Internal // todo do we want to invalidate test results on host change?
    public abstract val baseUrl: Property<String>

    @get:Internal // todo do we want to invalidate test results on timeout increase?
    public abstract val testTimeout: Property<Duration>

    @get:InputDirectory
    public abstract val apk: DirectoryProperty

    @get:InputDirectory
    public abstract val testApk: DirectoryProperty

    @get:Input
    public abstract val configTestMode: Property<Boolean>

    @get:OutputDirectory
    public abstract val outputDir: DirectoryProperty

    @ExperimentalTime
    @TaskAction
    public fun action() {

        val artifactory = artifactory.get()

        val artifactorySettings = ArtifactorySettings(
            baseUrl = artifactory.baseUrl.get(),
            user = artifactory.user.get(),
            password = artifactory.password.get(),
            repository = artifactory.repository.get(),
        )

        val emceeTestAction = EmceeTestActionFactory(
            emceeQueueBaseUrl = baseUrl.get(),
            artifactorySettings = artifactorySettings
        ).create()

        val job = job.get()
        val testTimeoutInSec = testTimeout.get().seconds

        val config = EmceeTestActionConfig(
            job = Job(
                id = job.id.get(),
                groupId = job.groupId.get(),
                priority = job.priority.get(),
                groupPriority = job.groupPriority.get(),
            ),
            artifactory = artifactorySettings,
            scheduleStrategy = ScheduleStrategy(testsSplitter = ScheduleStrategy.TestsSplitter.Individual),
            testExecutionBehavior = TestExecutionBehavior(
                environment = emptyMap(),
                retries = retries.get()
            ),
            testMaximumDurationSec = testTimeoutInSec,
            devices = deviceApis.get().map { api -> DeviceConfiguration("", api) },
            apk = apk.get().getApkOrThrow(), // todo should be optional for libraries
            testApk = testApk.get().getApkOrThrow()
        )

        // writing config dump every time for debug purposes
        EmceeConfigTestHelper(outputDir.get().asFile).serialize(config)

        if (!configTestMode.get()) {
            emceeTestAction.execute(config)
        }
    }
}
