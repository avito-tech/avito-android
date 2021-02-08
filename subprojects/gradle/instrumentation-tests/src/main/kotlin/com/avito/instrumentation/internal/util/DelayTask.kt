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
public abstract class DelayAction : WorkAction<Parameters> {

    public interface Parameters : WorkParameters {
        public fun getMillis(): Property<Long>
    }

    override fun execute() {
        Thread.sleep(parameters.getMillis().get())
    }
}

public abstract class DelayTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @Internal
    public val delayMillis: Property<Long> = project.objects.property()

    @Suppress("UnstableApiUsage")
    @TaskAction
    public fun action() {
        workerExecutor.noIsolation().submit(DelayAction::class.java) { parameters ->
            parameters.getMillis().set(delayMillis)
        }
    }
}
