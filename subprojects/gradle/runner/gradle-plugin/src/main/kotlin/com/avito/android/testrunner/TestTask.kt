package com.avito.android.testrunner

import com.avito.android.testrunner.service.TestService
import com.avito.gradle.worker.inMemoryWork
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

public abstract class TestTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @get:Internal
    public abstract val service: Property<TestService>

    @TaskAction
    public fun doWork() {
        val testService = service.get()

        workerExecutor.inMemoryWork {
            when (testService.runTests()) {
                is TestService.TestServiceRunResult.Success -> TODO()
                is TestService.TestServiceRunResult.Error -> TODO()
            }
        }
    }
}
