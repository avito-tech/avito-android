package com.avito.performance

import com.avito.utils.logging.ciLogger
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class SendPerformanceMdeTask @Inject constructor(
    objects: ObjectFactory,
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @get:InputFile
    internal val currentTests: RegularFileProperty = objects.fileProperty()

    @get:Internal
    internal val statsUrl = objects.property<String>()

    @TaskAction
    fun action() {

        //todo новое api, когда выйдет в stable
        // https://docs.gradle.org/5.6/userguide/custom_tasks.html#using-the-worker-api
        @Suppress("DEPRECATION")
        workerExecutor.submit(SendPerformanceMdeAction::class.java) { workerConfiguration ->
            workerConfiguration.isolationMode = IsolationMode.NONE
            workerConfiguration.setParams(
                SendPerformanceMdeAction.Params(
                    currentTests = currentTests.asFile.get(),
                    logger = ciLogger,
                    url = statsUrl.get()
                )
            )
        }
    }
}
