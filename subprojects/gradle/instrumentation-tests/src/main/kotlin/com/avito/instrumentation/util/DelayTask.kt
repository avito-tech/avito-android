package com.avito.instrumentation.util

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerConfiguration
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

class DelayAction(private val millis: Long) : Runnable {

    override fun run() {
        Thread.sleep(millis)
    }
}

class DelayTask @Inject constructor(private val workerExecutor: WorkerExecutor) : DefaultTask() {

    @Internal
    val delayMillis = project.objects.property<Long>()

    @TaskAction
    fun action() {
        workerExecutor.submit(DelayAction::class.java) { workerConfiguration: WorkerConfiguration ->
            workerConfiguration.isolationMode = IsolationMode.NONE
            workerConfiguration.setParams(delayMillis.get())
        }
    }
}
