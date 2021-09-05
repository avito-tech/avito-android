package com.avito.android.build_checks.internal

import com.avito.android.build_checks.RootProjectChecksExtension
import com.avito.android.build_checks.internal.MacOSLocalhostResolvingTask.Action.Parameters
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.system.measureTimeMillis

internal abstract class MacOSLocalhostResolvingTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @Input
    val today =
        project.objects.property<String>().convention(SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date()))

    @OutputFile
    val result =
        project.objects.fileProperty().apply { set(project.layout.buildDirectory.file("mac-os-localhost.output")) }

    @TaskAction
    fun check() {
        workerExecutor.noIsolation().submit(Action::class.java) { params ->
            params.today.set(today)
            params.output.set(result)
        }
    }

    abstract class Action : WorkAction<Parameters> {

        interface Parameters : WorkParameters {
            val today: Property<String>
            val output: RegularFileProperty
        }

        override fun execute() {
            val resolveTimeMs = averageTimeMs(count = 3) { resolveLocalhost() }

            if (resolveTimeMs > RESOLUTION_THRESHOLD_MS) {
                throw GradleException(
                    FailedCheckMessage(
                        RootProjectChecksExtension::macOSLocalhost,
                        """
                            Localhost resolution took $resolveTimeMs ms.
                            This is a bug in JVM on macOS. Please fix it: https://thoeni.io/post/macos-sierra-java/
                        """
                    ).toString()
                )
            } else {
                parameters.output.get().asFile.writeText(
                    parameters.today.get()
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

private const val RESOLUTION_THRESHOLD_MS = 100
