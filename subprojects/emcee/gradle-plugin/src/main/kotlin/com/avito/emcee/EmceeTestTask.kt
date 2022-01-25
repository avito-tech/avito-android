package com.avito.emcee

import com.avito.emcee.internal.com.avito.emcee.EmceeTestActionConfig
import com.avito.emcee.queue.Device
import com.avito.emcee.queue.Job
import com.avito.emcee.queue.ScheduleStrategy
import com.avito.emcee.queue.TestExecutionBehavior
import com.avito.emcee.queue.TestTimeoutConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import java.time.Duration
import kotlin.time.ExperimentalTime

public abstract class EmceeTestTask : DefaultTask() {

    @get:Nested
    public abstract val job: Property<JobConfiguration>

    @get:Input
    public abstract val retries: Property<Int>

    @get:Input
    public abstract val deviceApis: ListProperty<Int>

    @get:Internal
    public abstract val testTimeout: Property<Duration>

    @get:InputFile
    public abstract val apk: Property<RegularFile>

    @get:InputFile
    public abstract val testApk: Property<RegularFile>

    @ExperimentalTime
    @TaskAction
    public fun action() {
        val emceeTestAction = EmceeTestActionFactory().create()
        val job = job.get()
        val testTimeoutInSec = testTimeout.get().seconds

        emceeTestAction.execute(
            EmceeTestActionConfig(
                job = Job(
                    id = job.id.get(),
                    groupId = job.groupId.get(),
                    priority = job.priority.get(),
                    groupPriority = job.groupPriority.get(),
                    analyticsConfiguration = Any()
                ),
                scheduleStrategy = ScheduleStrategy(testsSplitter = ScheduleStrategy.TestsSplitter.Individual),
                testExecutionBehavior = TestExecutionBehavior(
                    environment = emptyMap(),
                    retries = retries.get()
                ),
                timeoutConfiguration = TestTimeoutConfiguration(testTimeoutInSec.toFloat(), testTimeoutInSec.toFloat()),
                devices = deviceApis.get().map { api -> Device("", api.toString()) },
                apk = apk.get().asFile,
                testApk = testApk.get().asFile
            )
        )
    }
}
