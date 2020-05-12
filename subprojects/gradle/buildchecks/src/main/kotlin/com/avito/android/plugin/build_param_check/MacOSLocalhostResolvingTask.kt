package com.avito.android.plugin.build_param_check

import com.avito.android.plugin.build_param_check.MacOSLocalhostResolvingTask.Action.Parameters
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import java.net.InetAddress
import javax.inject.Inject
import kotlin.system.measureTimeMillis

abstract class MacOSLocalhostResolvingTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @TaskAction
    fun check() {
        @Suppress("UnstableApiUsage")
        workerExecutor.noIsolation().submit(Action::class.java) {}
    }

    @Suppress("UnstableApiUsage")
    abstract class Action : WorkAction<Parameters> {

        interface Parameters : WorkParameters

        override fun execute() {
            val resolveTimeMs = averageTimeMs(count = 3) { resolveLocalhost() }

            if (resolveTimeMs > 100) {
                throw GradleException(
                    FailedCheckMessage(
                        BuildChecksExtension::macOSLocalhost, """
                            Localhost resolution took $resolveTimeMs ms.
                            This is a bug in JVM on macOS. Please fix it: https://thoeni.io/post/macos-sierra-java/
                            """
                    ).toString()
                )
            }
        }

        private fun averageTimeMs(warmups: Int = 1, count: Int, action: () -> Unit): Long {
            repeat(warmups) { action() }

            return measureTimeMillis {
                repeat(count) { action() }
            } / count
        }

        private fun resolveLocalhost() = InetAddress.getLocalHost().hostName

    }
}

