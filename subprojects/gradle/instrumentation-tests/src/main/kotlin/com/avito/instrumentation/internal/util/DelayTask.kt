package com.avito.instrumentation.util

import com.avito.instrumentation.util.DelayAction.Parameters
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class DelayAction : WorkAction<Parameters> {

    interface Parameters : WorkParameters {
        fun getMillis(): Property<Long>
    }

    override fun execute() {
        Thread.sleep(parameters.getMillis().get())
    }
}

abstract class DelayTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @Internal
    val delayMillis = project.objects.property<Long>()

    @Suppress("UnstableApiUsage")
    @TaskAction
    fun action() {
        workerExecutor.noIsolation().submit(DelayAction::class.java) { parameters ->
            parameters.getMillis().set(delayMillis)
        }
    }
}
